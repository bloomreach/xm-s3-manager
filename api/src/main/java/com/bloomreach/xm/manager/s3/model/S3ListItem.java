/*
 * Copyright 2020-2025 Bloomreach (http://www.bloomreach.com)
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

import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.tika.Tika;

import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.bloomreach.xm.manager.api.ListItem;
import com.bloomreach.xm.manager.api.Type;
import com.bloomreach.xm.manager.util.Util;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class S3ListItem implements ListItem {

    private static final Tika TIKA = new Tika();
    private String id;
    private String path;
    private String name;
    private Date lastModified;
    private Long size;
    private String humanReadableSize;
    private Type type;
    private String link;

    public S3ListItem() {
    }

    public S3ListItem(final S3ObjectSummary object, final String link) {
        this.id = object.getKey();
        this.name = Util.getLastBitFromUrl(object.getKey());
        this.lastModified = object.getLastModified();
        this.size = object.getSize();
        this.humanReadableSize = FileUtils.byteCountToDisplaySize(object.getSize());
        this.type = Type.FILE;
        this.link = link;
        this.path = object.getKey();
        String detect = TIKA.detect(object.getKey());
        if (detect.startsWith("image")) {
            this.type = Type.IMAGE;
        }
    }

    public S3ListItem(final String folder) {
        this.id = folder;
        this.path = folder;
        this.name = Util.getLastBitFromUrl(folder);
        this.type = Type.FOLDER;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Date getLastModified() {
        return lastModified;
    }

    @Override
    public Long getSize() {
        return size;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String getLink() {
        return link;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getHumanReadableSize() {
        return humanReadableSize;
    }
}
