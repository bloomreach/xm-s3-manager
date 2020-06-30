package com.bloomreach.xm.manager.common.s3.service;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;

public class AwsCredentials {

    private final AWSCredentials credentials;

    public AwsCredentials(final String accessKey, final String secretKey) {
        credentials = new BasicAWSCredentials(
                accessKey,
                secretKey
        );
    }

    public AWSCredentials getCredentials() {
        return credentials;
    }
}
