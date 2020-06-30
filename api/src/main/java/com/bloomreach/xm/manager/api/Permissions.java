package com.bloomreach.xm.manager.api;

public interface Permissions {

    public boolean isUseAllowed();

    public boolean isUploadAllowed();

    public boolean isDeleteAllowed();

    public boolean isCreateAllowed();
}
