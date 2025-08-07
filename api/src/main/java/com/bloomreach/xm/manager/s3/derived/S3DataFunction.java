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
package com.bloomreach.xm.manager.s3.derived;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.Value;

import com.bloomreach.xm.manager.s3.model.S3ListItem;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.StringUtils;
import org.hippoecm.repository.ext.DerivedDataFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S3DataFunction extends DerivedDataFunction {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(S3DataFunction.class);
    private static final String ASSETS = "assets";
    private static final String ASSET_IDS = "assetids";

    static {
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public Map<String, Value[]> compute(final Map<String, Value[]> map) {
        try {
            if (map.containsKey(ASSETS) && map.get(ASSETS) != null) {
                String openuidString = map.get(ASSETS)[0].getString();
                if (StringUtils.isNotEmpty(openuidString)) {
                    List<S3ListItem> items = MAPPER.readValue(openuidString, MAPPER.getTypeFactory().constructCollectionType(List.class, S3ListItem.class));
                    Value[] values = items.stream()
                            .map(s3ListItem -> getValueFactory().createValue(s3ListItem.getId()))
                            .toArray(Value[]::new);
                    map.put(ASSET_IDS, values);
                } else {
                    map.put(ASSET_IDS, new Value[]{});
                }
            }
        } catch (RepositoryException | IOException e) {
            log.error("error while trying to derive ids for S3 assets.", e);
        }
        return map;
    }
}

