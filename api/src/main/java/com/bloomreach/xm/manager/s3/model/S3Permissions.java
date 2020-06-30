package com.bloomreach.xm.manager.s3.model;

import com.bloomreach.xm.manager.api.Permissions;

public class S3Permissions implements Permissions {

    private boolean isUseAllowed;
    private boolean isUploadAllowed;
    private boolean isDeleteAllowed;
    private boolean isCreateAllowed;

    public S3Permissions(final boolean isUseAllowed, final boolean isUploadAllowed, final boolean isDeleteAllowed, final boolean isCreateAllowed) {
        this.isUseAllowed = isUseAllowed;
        this.isUploadAllowed = isUploadAllowed;
        this.isDeleteAllowed = isDeleteAllowed;
        this.isCreateAllowed = isCreateAllowed;
    }

    @Override
    public boolean isUseAllowed() {
        return isUseAllowed;
    }

    @Override
    public boolean isUploadAllowed() {
        return isUploadAllowed;
    }

    @Override
    public boolean isDeleteAllowed() {
        return isDeleteAllowed;
    }

    @Override
    public boolean isCreateAllowed() {
        return isCreateAllowed;
    }
}
