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
