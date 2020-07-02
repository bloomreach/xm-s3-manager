/*
 * Copyright 2020 Bloomreach (http://www.bloomreach.com)
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
package com.bloomreach.xm.manager;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.onehippo.cms7.services.HippoServiceRegistry;
import org.onehippo.repository.jaxrs.CXFRepositoryJaxrsEndpoint;
import org.onehippo.repository.jaxrs.RepositoryJaxrsService;
import org.onehippo.repository.jaxrs.api.ManagedUserSessionInvoker;
import org.onehippo.repository.modules.AbstractReconfigurableDaemonModule;
import org.onehippo.repository.modules.ProvidesService;

import com.bloomreach.xm.manager.common.api.AwsS3Service;
import com.bloomreach.xm.manager.s3.service.AwsCredentials;
import com.bloomreach.xm.manager.s3.service.AwsS3ServiceImpl;
import com.bloomreach.xm.manager.s3.service.AwsService;
import com.bloomreach.xm.manager.s3.controller.AwsS3ProxyController;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

@ProvidesService(types = {AwsS3Service.class})
public class S3ManagerDaemonModule extends AbstractReconfigurableDaemonModule {

    private static final String END_POINT = "/s3manager";
    private AwsS3Service awsS3Service;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private boolean presigned;
    private long expTime;

    @Override
    protected void doConfigure(final Node moduleConfig) throws RepositoryException {
        accessKey = moduleConfig.getProperty("accessKey").getString();
        secretKey = moduleConfig.getProperty("secretKey").getString();
        bucket = moduleConfig.getProperty("bucket").getString();
        presigned = moduleConfig.getProperty("presigned").getBoolean();
        if(moduleConfig.hasProperty("expirationTime")) {
            expTime = moduleConfig.getProperty("expirationTime").getLong();
        }
    }

    @Override
    public void doInitialize(final Session session) throws RepositoryException {
        AwsCredentials awsCredentials = new AwsCredentials(accessKey, secretKey);
        AwsService awsService = new AwsService(awsCredentials);
        awsS3Service = new AwsS3ServiceImpl(awsService, bucket, presigned, expTime);
        AwsS3ProxyController awsS3ProxyController = new AwsS3ProxyController((AwsS3ServiceImpl) awsS3Service, session);

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
        doShutdown();
        doConfigure(moduleConfig);
        doInitialize(moduleConfig.getSession());
    }

}