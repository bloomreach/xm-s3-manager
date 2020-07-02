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
package com.bloomreach.xm.manager.beans;

import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoCompoundBean;
import org.hippoecm.hst.content.beans.standard.HippoItem;
import org.onehippo.cms7.essentials.dashboard.annotations.HippoEssentialsGenerated;
import org.onehippo.cms7.services.HippoServiceRegistry;

import com.bloomreach.xm.manager.common.api.AwsS3Service;

@Node(jcrType = "s3manager:s3managerpicker")
@HippoEssentialsGenerated(internalName = "s3manager:s3managerpicker", allowModifications = false)
public class S3managerpicker extends HippoItem implements HippoCompoundBean {

    @HippoEssentialsGenerated(internalName = "s3manager:assets", allowModifications = false)
    public String getAssets() {
        return getSingleProperty("s3manager:assets");
    }

    @HippoEssentialsGenerated(internalName = "s3manager:assetids", allowModifications = false)
    public String[] getAssetids() {
        String[] assetIDs = getMultipleProperty("s3manager:assetids");
        for(int i = 0; i < assetIDs.length; i++){
            AwsS3Service awsS3Service = HippoServiceRegistry.getService(AwsS3Service.class);
            assetIDs[i] = awsS3Service.generateUrl(assetIDs[i]);
        }
        return assetIDs;
    }
}
