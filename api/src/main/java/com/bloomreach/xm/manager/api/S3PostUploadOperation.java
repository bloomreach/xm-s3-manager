package com.bloomreach.xm.manager.api;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.internal.SSEResultBase;

public interface S3PostUploadOperation<T extends SSEResultBase> {

    void process(final AmazonS3 amazonS3, final T result);
}
