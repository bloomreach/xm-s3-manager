package com.bloomreach.xm.manager.s3.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class AwsService {

    private final AmazonS3 s3client;

    public AwsService(final AwsCredentials credentials) {
        s3client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials.getCredentials()))
                .withRegion(Regions.EU_WEST_1)
                .build();
    }

    public AmazonS3 getS3client() {
        return s3client;
    }
}
