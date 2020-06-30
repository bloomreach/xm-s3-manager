package com.bloomreach.xm.manager.rewriter;

import javax.jcr.Node;

import org.hippoecm.hst.configuration.hosting.Mount;
import org.hippoecm.hst.content.rewriter.impl.SimpleContentRewriter;
import org.hippoecm.hst.core.request.HstRequestContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.onehippo.cms7.services.HippoServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bloomreach.xm.manager.common.api.AwsS3Service;

public class S3AssetsLinkRewriter extends SimpleContentRewriter {

    private final static Logger log = LoggerFactory.getLogger(S3AssetsLinkRewriter.class);

    private static boolean awsS3ServiceInitialized;
    private static AwsS3Service awsS3Service;

    private static synchronized void initService() {
        if (!awsS3ServiceInitialized) {
            awsS3Service = HippoServiceRegistry.getService(AwsS3Service.class);
            awsS3ServiceInitialized = true;
        }
    }

    protected static AwsS3Service getAwsS3Service() {
        if (!awsS3ServiceInitialized) {
            initService();
        }
        return awsS3Service;
    }

    @Override
    public String rewrite(final String html, final Node hippoHtmlNode, final HstRequestContext requestContext, final Mount targetMount) {
        if (html == null) {
            return null;
        }

        Document document = Jsoup.parse(html);
        Elements s3elements = document.getElementsByAttribute("data-s3id");
        s3elements.forEach(element -> {
            String key = element.attr("data-s3id");
            String url = getAwsS3Service().generateUrl(key);

            String elementTagName = element.tagName();
            if("a".equals(elementTagName)) {
                element.attr("href", url);
            } else if ("img".equals(elementTagName)) {
                element.attr("src", url);
            }
        });

        return super.rewrite(document.html(), hippoHtmlNode, requestContext, targetMount);
    }
}
