/*
 * Copyright 2020-2025 Bloomreach (http://www.bloomreach.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bloomreach.xm.manager.s3.module;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang3.StringUtils;
import org.onehippo.cms7.services.HippoServiceRegistry;
import org.onehippo.repository.jaxrs.CXFRepositoryJaxrsEndpoint;
import org.onehippo.repository.jaxrs.RepositoryJaxrsService;
import org.onehippo.repository.jaxrs.api.ManagedUserSessionInvoker;
import org.onehippo.repository.modules.AbstractReconfigurableDaemonModule;
import org.onehippo.repository.modules.ProvidesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bloomreach.xm.manager.common.api.AwsS3Service;
import com.bloomreach.xm.manager.s3.controller.AwsS3ProxyController;
import com.bloomreach.xm.manager.s3.model.DZConfiguration;
import com.bloomreach.xm.manager.s3.service.AwsS3ServiceImpl;
import com.bloomreach.xm.manager.s3.service.AwsService;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.ListMultipartUploadsRequest;
import software.amazon.awssdk.services.s3.model.ListMultipartUploadsResponse;
import software.amazon.awssdk.services.s3.model.MultipartUpload;

@ProvidesService(types = {AwsS3Service.class})
public class S3ManagerDaemonModule extends AbstractReconfigurableDaemonModule {

    private static final Logger logger = LoggerFactory.getLogger(S3ManagerDaemonModule.class);
    private static final String END_POINT = "/s3manager";
    private static final String XM_S3_ACCESS_KEY = "XM_S3_ACCESS_KEY";
    private static final String XM_S3_SECRET_KEY = "XM_S3_SECRET_KEY";
    private AwsService awsService;
    private AwsS3Service awsS3Service;
    private String accessKey;
    private String secretKey;
    private String region;
    private String bucket;
    private boolean presigned;
    private boolean aclEnabled;
    private long expTime;
    private DZConfiguration dzConfiguration;

    @Override
    protected void doConfigure(final Node moduleConfig) throws RepositoryException {
        if(StringUtils.isNotEmpty(System.getenv(XM_S3_ACCESS_KEY))){
            accessKey = System.getenv(XM_S3_ACCESS_KEY);
        } else if (StringUtils.isNotEmpty(System.getProperty(XM_S3_ACCESS_KEY))){
            accessKey = System.getProperty(XM_S3_ACCESS_KEY);
        } else if (moduleConfig.hasProperty("accessKey")) {
            accessKey = moduleConfig.getProperty("accessKey").getString();
        }

        if(StringUtils.isNotEmpty(System.getenv(XM_S3_SECRET_KEY))){
            secretKey = System.getenv(XM_S3_SECRET_KEY);
        } else if (StringUtils.isNotEmpty(System.getProperty(XM_S3_SECRET_KEY))){
            secretKey = System.getProperty(XM_S3_SECRET_KEY);
        } else if (moduleConfig.hasProperty("secretKey")) {
            secretKey = moduleConfig.getProperty("secretKey").getString();
        }

        bucket = moduleConfig.getProperty("bucket").getString();
        region = moduleConfig.getProperty("region").getString();
        presigned = moduleConfig.getProperty("presigned").getBoolean();
        aclEnabled = !moduleConfig.hasProperty("aclEnabled") || moduleConfig.getProperty("aclEnabled").getBoolean();

        if(moduleConfig.hasProperty("expirationTime")) {
            expTime = moduleConfig.getProperty("expirationTime").getLong();
        }

        extractDZConfiguration(moduleConfig);
    }

    private void extractDZConfiguration(final Node moduleConfig) throws RepositoryException {
        String allowedExtensions = moduleConfig.hasProperty("allowedExtensions") ?
                Arrays.stream(moduleConfig.getProperty("allowedExtensions").getValues()).map(value -> {
                    try {
                        return value.getString();
                    } catch (RepositoryException e) {
                        logger.error("An exception occurred while reading dz configuration for allowed extensions.", e);
                    }
                    return null;
                }).collect(Collectors.joining(",")) : null;

        long maxFileSize = moduleConfig.hasProperty("maxFileSize") ?
                moduleConfig.getProperty("maxFileSize").getValue().getLong() : 160000;

        long chunkSize = moduleConfig.hasProperty("chunkSize") ?
            moduleConfig.getProperty("chunkSize").getValue().getLong() : 5;

        long timeout = moduleConfig.hasProperty("timeout") ?
            moduleConfig.getProperty("timeout").getValue().getLong() : 0;

        dzConfiguration = new DZConfiguration(allowedExtensions, maxFileSize, chunkSize, timeout);
    }

    @Override
    public void doInitialize(final Session session) throws RepositoryException {
        AwsCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
        awsService = new AwsService(awsCredentials, region);
        awsS3Service = new AwsS3ServiceImpl(awsService, bucket, region, presigned, aclEnabled, expTime);
        AwsS3ProxyController awsS3ProxyController = new AwsS3ProxyController((AwsS3ServiceImpl) awsS3Service, session, dzConfiguration);

        HippoServiceRegistry.register(awsS3Service, AwsS3Service.class);
        ManagedUserSessionInvoker managedUserSessionInvoker = new ManagedUserSessionInvoker(session);

        RepositoryJaxrsService.addEndpoint(new CXFRepositoryJaxrsEndpoint(END_POINT)
                .invoker(managedUserSessionInvoker)
                .singleton(awsS3ProxyController)
                .singleton(new JacksonJsonProvider())
        );
    }

    @Override
    protected void doShutdown() {
        if (awsS3Service != null) {
            HippoServiceRegistry.unregister(awsS3Service, AwsS3Service.class);
        }
        RepositoryJaxrsService.removeEndpoint(END_POINT);
    }

    @Override
    protected void onConfigurationChange(final Node moduleConfig) throws RepositoryException {
        try {
            ListMultipartUploadsRequest allMultipartUploadsRequest = ListMultipartUploadsRequest.builder().bucket(bucket).build();
            ListMultipartUploadsResponse response = awsService.getS3client().listMultipartUploads(allMultipartUploadsRequest);
            List<MultipartUpload> uploads = response.uploads();
            for (MultipartUpload u : uploads) {
                AbortMultipartUploadRequest abortRequest = AbortMultipartUploadRequest.builder()
                    .bucket(bucket)
                    .key(u.key())
                    .uploadId(u.uploadId())
                    .build();
                awsService.getS3client().abortMultipartUpload(abortRequest);
            }
        } catch (SdkClientException e) {
            logger.error("An exception occurred while aborting in-progress multi part uploads before module reconfiguration.", e);
        } finally {
            doShutdown();
            doConfigure(moduleConfig);
            doInitialize(moduleConfig.getSession());
        }
    }

}
