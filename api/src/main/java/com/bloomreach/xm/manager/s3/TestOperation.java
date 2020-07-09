package com.bloomreach.xm.manager.s3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.bloomreach.xm.manager.api.S3PostUploadOperation;

public class TestOperation implements S3PostUploadOperation<PutObjectResult> {

    private static final Logger logger = LoggerFactory.getLogger(TestOperation.class);

    @Override
    public void process(final AmazonS3 amazonS3, final PutObjectResult result) {
        logger.debug("Completed upload for {}", result.getETag());
    }
}
