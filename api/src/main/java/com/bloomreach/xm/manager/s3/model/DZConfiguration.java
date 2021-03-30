package com.bloomreach.xm.manager.s3.model;

public class DZConfiguration {

    private String allowedExtensions;
    private long maxFileSize;
    private long chunkSize;
    private long timeout;

    public DZConfiguration(final String allowedExtensions, final long maxFileSize, final long chunkSize, final long timeout) {
        this.allowedExtensions = allowedExtensions;
        this.maxFileSize = maxFileSize;
        this.chunkSize = chunkSize;
        this.timeout = timeout;
    }

    public String getAllowedExtensions() {
        return allowedExtensions;
    }

    public void setAllowedExtensions(final String allowedExtensions) {
        this.allowedExtensions = allowedExtensions;
    }

    public long getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(final long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public long getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(final long chunkSize) {
        this.chunkSize = chunkSize;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(final long timeout) {
        this.timeout = timeout;
    }
}
