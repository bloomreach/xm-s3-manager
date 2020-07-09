package com.bloomreach.xm.manager.s3.operations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.bloomreach.xm.manager.api.PostUploadOperation;

public class TestOperation implements PostUploadOperation<PutObjectResult> {

    private static final Logger logger = LoggerFactory.getLogger(TestOperation.class);
    private static AmazonS3 client;

    public TestOperation(final AmazonS3 client) {
        TestOperation.client = client;
    }

    @Override
    public void process(final PutObjectResult result, final String... key) {
        logger.debug("Completed upload for {} with etag: {}", key, result.getETag());
    }
}
