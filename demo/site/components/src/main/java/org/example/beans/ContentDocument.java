package org.example.beans;

import java.util.Calendar;
import org.hippoecm.hst.content.beans.Node;
import org.hippoecm.hst.content.beans.standard.HippoHtml;
import org.onehippo.cms7.essentials.dashboard.annotations.HippoEssentialsGenerated;
import com.bloomreach.xm.manager.beans.S3managerpicker;

@HippoEssentialsGenerated(internalName = "demo:contentdocument")
@Node(jcrType = "demo:contentdocument")
public class ContentDocument extends BaseDocument {
    @HippoEssentialsGenerated(internalName = "demo:introduction")
    public String getIntroduction() {
        return getSingleProperty("demo:introduction");
    }

    @HippoEssentialsGenerated(internalName = "demo:title")
    public String getTitle() {
        return getSingleProperty("demo:title");
    }

    @HippoEssentialsGenerated(internalName = "demo:content")
    public HippoHtml getContent() {
        return getHippoHtml("demo:content");
    }

    @HippoEssentialsGenerated(internalName = "demo:publicationdate")
    public Calendar getPublicationDate() {
        return getSingleProperty("demo:publicationdate");
    }

    @HippoEssentialsGenerated(internalName = "demo:assets", allowModifications = false)
    public S3managerpicker getAssets() {
        return getBean("demo:assets", S3managerpicker.class);
    }
}
