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
            log.debug("Successfully retrieved AwsS3Service from HippoServiceRegistry.");
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
            log.debug("Html string received was empty so returning null.");
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
