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
package com.bloomreach.xm.manager.s3.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.tika.Tika;
import org.onehippo.repository.security.SessionUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;
import com.bloomreach.xm.manager.api.ListItem;
import com.bloomreach.xm.manager.api.Type;
import com.bloomreach.xm.manager.common.api.AwsS3Service;
import com.bloomreach.xm.manager.s3.model.S3ListItem;

public class AwsS3ServiceImpl implements AwsS3Service {

    private static final Logger logger = LoggerFactory.getLogger(AwsS3ServiceImpl.class);
    private static final Tika TIKA = new Tika();
    private final String bucket;
    private final AmazonS3 amazonS3;
    private final boolean presigned;
    private final boolean aclEnabled;
    private final long expTime;
    private final Map<String, InitiateMultipartUploadResult> multipartUploadResultMap = new HashMap<>();
    private final MultiValueMap<String, PartETag> eParts = new LinkedMultiValueMap<>();

    public AwsS3ServiceImpl(final AwsService awsService, final String bucket, final boolean presigned, final boolean aclEnabled, final long expTime) {
        this.bucket = bucket;
        amazonS3 = awsService.getS3client();
        this.presigned = presigned;
        this.aclEnabled = aclEnabled;
        this.expTime = expTime;
    }

    @Override
    public String generateUrl(final String key) {
        if(!presigned) {
            return amazonS3.getUrl(bucket, key).toString();
        } else {
            return generatePresignedUrl(key);
        }
    }

    public String generatePresignedUrl(final String key) {
        java.util.Date expiration = new java.util.Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * expTime;
        expiration.setTime(expTimeMillis);

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucket, key)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);
        final URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
        return url.toString();
    }

    public List<S3ListItem> getList(final String prefix, final String query) {
        ListObjectsV2Request listObjectsRequest = new ListObjectsV2Request()
                .withBucketName(bucket)
                .withPrefix(prefix)
                .withDelimiter("/");

        ListObjectsV2Result objectListing = amazonS3.listObjectsV2(listObjectsRequest);

        List<S3ListItem> objects = objectListing.getObjectSummaries().stream().filter(s3ObjectSummary -> !s3ObjectSummary.getKey().equals(prefix)).map(s3ObjectSummary -> new S3ListItem(s3ObjectSummary, amazonS3.getUrl(bucket, s3ObjectSummary.getKey()).toString())).collect(Collectors.toList());
        List<S3ListItem> folders = objectListing.getCommonPrefixes().stream().map(S3ListItem::new).collect(Collectors.toList());
        return Stream.concat(folders.stream(), objects.stream()).filter(s3ListItem -> StringUtils.isEmpty(query) || s3ListItem.getName().toLowerCase().contains(query.toLowerCase())).collect(Collectors.toList());
    }

    public void createFolder(String key) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("binary/octet-stream");
        PutObjectRequest putRequest = new PutObjectRequest(bucket, key + "/", new ByteArrayInputStream(new byte[0]), metadata);
        amazonS3.putObject(putRequest);
    }

    public void deleteFiles(List<S3ListItem> items) {
        DeleteObjectsRequest deleteObjectsRequestFiles = new DeleteObjectsRequest(bucket);
        String[] filesKeys = items.stream().filter(s3ListItem -> s3ListItem.getType().equals(Type.FILE) || s3ListItem.getType().equals(Type.IMAGE)).map(ListItem::getId).toArray(String[]::new);
        if (filesKeys.length > 0) {
            deleteObjectsRequestFiles.withKeys(filesKeys);
            amazonS3.deleteObjects(deleteObjectsRequestFiles);
        }
        items.stream().filter(s3ListItem -> s3ListItem.getType().equals(Type.FOLDER)).map(S3ListItem::getId).forEach(this::deleteDirectory);
    }

    public void deleteDirectory(String prefix) {
        ObjectListing objectList = amazonS3.listObjects(bucket, prefix);
        String[] keysList = objectList.getObjectSummaries().stream().map(S3ObjectSummary::getKey).toArray(String[]::new);
        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucket).withKeys(keysList);
        amazonS3.deleteObjects(deleteObjectsRequest);
    }

    //for small files
    public void uploadSinglepart(final SessionUser user, Attachment multipartFile, final String path) {
        String uniqueFileName = path + multipartFile.getDataHandler().getName();
        PutObjectRequest por = null;
        try {
            byte[] bytes = IOUtils.toByteArray(multipartFile.getDataHandler().getInputStream());
            String contentType = multipartFile.getContentType().toString();
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(contentType);
            objectMetadata.setUserMetadata(getUserMetadata(user));
            objectMetadata.setContentLength(bytes.length);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            por = new PutObjectRequest(bucket, uniqueFileName, byteArrayInputStream, objectMetadata);
            if(aclEnabled){
                por.withCannedAcl(CannedAccessControlList.PublicRead);
            }
        } catch (IOException e) {
            logger.error("An exception occurred during a single part upload to S3.", e);
        }
        amazonS3.putObject(por);
    }

    //for large files
    public void uploadMultipart(final SessionUser user, Attachment multipartFile, final String path, final int index, final int total) {
        String uniqueFileName = path + multipartFile.getDataHandler().getName();

        if(eParts.containsKey(uniqueFileName) && eParts.get(uniqueFileName).get(eParts.get(uniqueFileName).size()-1).getPartNumber() > index + 1){
            abortMultipartUploadAndClear(uniqueFileName);
        }

        if (!multipartUploadResultMap.containsKey(uniqueFileName)) {
            String contentType = TIKA.detect(uniqueFileName);
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(contentType);
            objectMetadata.setUserMetadata(getUserMetadata(user));
            InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(bucket, uniqueFileName, objectMetadata);
            if(aclEnabled){
                initRequest.withCannedACL(CannedAccessControlList.PublicRead);
            }
            InitiateMultipartUploadResult initResponse = amazonS3.initiateMultipartUpload(initRequest);
            multipartUploadResultMap.put(uniqueFileName, initResponse);
        }

        InitiateMultipartUploadResult initResponse = multipartUploadResultMap.get(uniqueFileName);

        int partNumber = index + 1;

        UploadPartRequest uploadRequest = null;
        try {
            byte[] bytes = IOUtils.toByteArray(multipartFile.getDataHandler().getInputStream());
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            uploadRequest = new UploadPartRequest()
                    .withBucketName(bucket)
                    .withKey(uniqueFileName)
                    .withUploadId(initResponse.getUploadId())
                    .withPartNumber(partNumber)
                    .withInputStream(byteArrayInputStream)
                    .withPartSize(bytes.length);
        } catch (IOException e) {
            logger.error("An exception occurred during a multi part upload to S3.", e);
        }
        // Upload the part and add the response's ETag to our list.
        UploadPartResult uploadResult = amazonS3.uploadPart(uploadRequest);
        eParts.add(uniqueFileName, uploadResult.getPartETag());

        if (partNumber == total) {
            CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(bucket, uniqueFileName,
                    initResponse.getUploadId(), eParts.get(uniqueFileName));
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
        InitiateMultipartUploadResult result = multipartUploadResultMap.get(uniqueFileName);
        if(result != null) {
            amazonS3.abortMultipartUpload(new AbortMultipartUploadRequest(bucket, result.getKey(), result.getUploadId()));
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
