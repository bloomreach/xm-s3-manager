package com.bloomreach.xm.manager.beans;

import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoCompound;
import org.onehippo.cms7.essentials.dashboard.annotations.HippoEssentialsGenerated;
import org.onehippo.cms7.services.HippoServiceRegistry;

import com.bloomreach.xm.manager.common.api.AwsS3Service;

@HippoEssentialsGenerated(internalName = "s3manager:s3managerpicker", allowModifications = false)
@Node(jcrType = "s3manager:s3managerpicker")
public class S3managerpicker extends HippoCompound {
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
