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
package com.bloomreach.xm.manager.s3.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.tika.Tika;
import org.onehippo.repository.security.SessionUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.bloomreach.xm.manager.api.Type;
import com.bloomreach.xm.manager.common.api.AwsS3Service;
import com.bloomreach.xm.manager.s3.model.S3ListItem;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.AbortMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CommonPrefix;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CreateMultipartUploadResponse;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.UploadPartRequest;
import software.amazon.awssdk.services.s3.model.UploadPartResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

public class AwsS3ServiceImpl implements AwsS3Service {

    private static final Logger logger = LoggerFactory.getLogger(AwsS3ServiceImpl.class);
    private static final Tika TIKA = new Tika();
    private final String bucket;
    private final Region region;
    private final S3Client amazonS3;
    private final S3Presigner presignerS3;
    private final boolean presigned;
    private final boolean aclEnabled;
    private final long expTime;
    private final Map<String, CreateMultipartUploadResponse> multipartUploadResultMap = new HashMap<>();
    private final MultiValueMap<String, CompletedPart> eParts = new LinkedMultiValueMap<>();

    public AwsS3ServiceImpl(final AwsService awsService, final String bucket, final String region, final boolean presigned, final boolean aclEnabled, final long expTime) {
        this.bucket = bucket;
        this.region = Region.of(region);
        amazonS3 = awsService.getS3client();
        presignerS3 = awsService.getS3presigner();
        this.presigned = presigned;
        this.aclEnabled = aclEnabled;
        this.expTime = expTime;
    }

    @Override
    public String generateUrl(final String key) {
        if(!presigned) {
            return getUrl(key);
        } else {
            return generatePresignedUrl(key);
        }
    }

    public String generatePresignedUrl(final String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(expTime))
            .getObjectRequest(getObjectRequest)
            .build();

        URL presignedUrl = presignerS3.presignGetObject(presignRequest).url();
        return presignedUrl.toString();
    }

    private String getUrl(final String key) {
      return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region.id(), key);
    }

    public List<S3ListItem> getList(final String prefix, final String query) {
        ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
              .bucket(bucket)
              .prefix(prefix)
              .delimiter("/")
              .build();

        ListObjectsV2Response objectListing = amazonS3.listObjectsV2(listObjectsRequest);

        List<S3ListItem> objects = objectListing.contents().stream().filter(s3Object -> !s3Object.key().equals(prefix)).map(s3Object -> new S3ListItem(s3Object, getUrl(s3Object.key()))).toList();
        List<S3ListItem> folders = objectListing.commonPrefixes().stream().map(CommonPrefix::prefix).map(S3ListItem::new).toList();
        return Stream.concat(folders.stream(), objects.stream()).filter(s3ListItem -> StringUtils.isEmpty(query) || s3ListItem.getName().toLowerCase().contains(query.toLowerCase())).collect(Collectors.toList());
    }

    public void createFolder(String key) {
        PutObjectRequest request = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key.endsWith("/") ? key : key + "/")
            .contentType("binary/octet-stream")
            .build();

        amazonS3.putObject(request, RequestBody.empty());
    }

    public void deleteFiles(List<S3ListItem> items) {
        List<ObjectIdentifier> keysToDelete = items.stream()
            .filter(s3ListItem -> s3ListItem.getType().equals(Type.FILE) || s3ListItem.getType().equals(Type.IMAGE))
            .map(s3ListItem -> ObjectIdentifier.builder().key(s3ListItem.getId()).build())
            .toList();

        if (!keysToDelete.isEmpty()) {
          DeleteObjectsRequest deleteObjectsRequest = DeleteObjectsRequest.builder()
              .bucket(bucket)
              .delete(Delete.builder().objects(keysToDelete).build())
              .build();

          amazonS3.deleteObjects(deleteObjectsRequest);
        }

        items.stream().filter(s3ListItem -> s3ListItem.getType().equals(Type.FOLDER)).map(S3ListItem::getId).forEach(this::deleteDirectory);
    }

    public void deleteDirectory(String prefix) {
        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
            .bucket(bucket)
            .prefix(prefix)
            .build();

        ListObjectsV2Response listResponse = amazonS3.listObjectsV2(listRequest);

        List<ObjectIdentifier> keysToDelete = listResponse.contents().stream()
            .map(s3Object -> ObjectIdentifier.builder().key(s3Object.key()).build())
            .collect(Collectors.toList());

        if (!keysToDelete.isEmpty()) {
          DeleteObjectsRequest deleteRequest = DeleteObjectsRequest.builder()
              .bucket(bucket)
              .delete(Delete.builder().objects(keysToDelete).build())
              .build();

          amazonS3.deleteObjects(deleteRequest);
        }
    }

    //for small files
    public void uploadSinglepart(final SessionUser user, Attachment multipartFile, final String path) {
        String uniqueFileName = path + multipartFile.getDataHandler().getName();
        try {
            byte[] bytes = IOUtils.toByteArray(multipartFile.getDataHandler().getInputStream());
            String contentType = multipartFile.getContentType().toString();

            PutObjectRequest.Builder requestBuilder = PutObjectRequest.builder()
                .bucket(bucket)
                .key(uniqueFileName)
                .contentType(contentType)
                .contentLength((long) bytes.length)
                .metadata(getUserMetadata(user));

            if (aclEnabled) {
              requestBuilder.acl(ObjectCannedACL.PUBLIC_READ);
            }

            PutObjectRequest putObjectRequest = requestBuilder.build();
            amazonS3.putObject(putObjectRequest, RequestBody.fromBytes(bytes));
        } catch (IOException e) {
            logger.error("An exception occurred during a single part upload to S3.", e);
        }
    }

    //for large files
    public void uploadMultipart(final SessionUser user, Attachment multipartFile, final String path, final int index, final int total) {
        String uniqueFileName = path + multipartFile.getDataHandler().getName();

        if(eParts.containsKey(uniqueFileName) && eParts.get(uniqueFileName).get(eParts.get(uniqueFileName).size()-1).partNumber() > index + 1){
            abortMultipartUploadAndClear(uniqueFileName);
        }

        if (!multipartUploadResultMap.containsKey(uniqueFileName)) {
            String contentType = TIKA.detect(uniqueFileName);

            CreateMultipartUploadRequest.Builder requestBuilder = CreateMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(uniqueFileName)
                .contentType(contentType)
                .metadata(getUserMetadata(user));

            if (aclEnabled) {
              requestBuilder.acl(ObjectCannedACL.PUBLIC_READ);
            }

            CreateMultipartUploadRequest request = requestBuilder.build();
            CreateMultipartUploadResponse response = amazonS3.createMultipartUpload(request);
            multipartUploadResultMap.put(uniqueFileName, response);
        }

        CreateMultipartUploadResponse initResponse = multipartUploadResultMap.get(uniqueFileName);

        int partNumber = index + 1;

        try {
            byte[] bytes = IOUtils.toByteArray(multipartFile.getDataHandler().getInputStream());
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            UploadPartRequest uploadRequest = UploadPartRequest.builder()
                .bucket(bucket)
                .key(uniqueFileName)
                .uploadId(initResponse.uploadId())
                .partNumber(partNumber)
                .contentLength((long) bytes.length)
                .build();

            // Upload the part and add the response's ETag to our list.
            UploadPartResponse uploadResponse = amazonS3.uploadPart(uploadRequest, RequestBody.fromInputStream(byteArrayInputStream, bytes.length));
            eParts.add(uniqueFileName, CompletedPart.builder()
                .partNumber(partNumber)
                .eTag(uploadResponse.eTag())
                .build());
        } catch (IOException e) {
            logger.error("An exception occurred during a multi part upload to S3.", e);
        }

        if (partNumber == total) {
            CompleteMultipartUploadRequest compRequest = CompleteMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(uniqueFileName)
                .uploadId(initResponse.uploadId())
                .multipartUpload(CompletedMultipartUpload.builder()
                    .parts(eParts.get(uniqueFileName))
                    .build())
                .build();
            try {
                amazonS3.completeMultipartUpload(compRequest);
            } catch (SdkClientException e){
                logger.error("An exception occurred while trying to finalise a multi part upload.", e);
            } finally {
                clearMultipartUpload(uniqueFileName);
            }
        }
    }

    public void abortMultipartUploadAndClear(final String uniqueFileName){
        CreateMultipartUploadResponse result = multipartUploadResultMap.get(uniqueFileName);
        if(result != null) {
            AbortMultipartUploadRequest abortRequest = AbortMultipartUploadRequest.builder()
                .bucket(bucket)
                .key(result.key())
                .uploadId(result.uploadId())
                .build();
            amazonS3.abortMultipartUpload(abortRequest);
        }
        clearMultipartUpload(uniqueFileName);
    }

    private Map<String, String> getUserMetadata(SessionUser user){
        Map<String, String> userMeta = new HashMap<>();
        userMeta.put("source", "brXM");
        if(StringUtils.isNotEmpty(user.getFirstName()) && StringUtils.isNotEmpty(user.getLastName())){
            userMeta.put("uploader", user.getFirstName()+" "+user.getLastName());
        } else {
            userMeta.put("uploader", user.getId());
        }
        return userMeta;
    }

    private void clearMultipartUpload(final String uniqueFileName) {
        multipartUploadResultMap.remove(uniqueFileName);
        eParts.remove(uniqueFileName);
    }

}
