package com.bloomreach.xm.manager.beans;

import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoCompound;
import org.onehippo.cms7.essentials.dashboard.annotations.HippoEssentialsGenerated;

//@HippoEssentialsGenerated(internalName = "s3manager:s3managerpicker")
@Node(jcrType = "s3manager:s3managerpicker")
public class S3managerpicker extends HippoCompound {
//    @HippoEssentialsGenerated(internalName = "s3manager:assets")
    public String getAssets() {
        return getSingleProperty("s3manager:assets");
    }

    //TODO Here invoke generatePresignedUrl
//    @HippoEssentialsGenerated(internalName = "s3manager:assetids")
    public String[] getAssetids() {
        return getMultipleProperty("s3manager:assetids");
    }
}
