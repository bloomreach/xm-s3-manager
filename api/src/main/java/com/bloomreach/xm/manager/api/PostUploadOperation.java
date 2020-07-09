package com.bloomreach.xm.manager.api;

public interface PostUploadOperation<T> {

    void process(final T result, final String... args);
}
