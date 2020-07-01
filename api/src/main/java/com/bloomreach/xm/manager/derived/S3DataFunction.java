package com.bloomreach.xm.manager.derived;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;
import javax.jcr.Value;

import com.bloomreach.xm.manager.s3.model.S3ListItem;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang.StringUtils;
import org.hippoecm.repository.ext.DerivedDataFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S3DataFunction extends DerivedDataFunction {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static Logger log = LoggerFactory.getLogger(S3DataFunction.class);

    static {
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public Map<String, Value[]> compute(final Map<String, Value[]> map) {
        try {
            if (map.containsKey("assets") && map.get("assets") != null) {
                String openuidString = map.get("assets")[0].getString();
                if (StringUtils.isNotEmpty(openuidString)) {
                    List<S3ListItem> items = MAPPER.readValue(openuidString, MAPPER.getTypeFactory().constructCollectionType(List.class, S3ListItem.class));
                    Value[] values = items.stream()
                            .map(s3ListItem -> getValueFactory().createValue(s3ListItem.getId()))
                            .toArray(Value[]::new);
                    map.put("assetids", values);
                } else {
                    map.put("assetids", new Value[]{});
                }
            }
        } catch (RepositoryException | IOException e) {
            log.error("error while trying to derive path for formio:", e);
        }
        return map;
    }
}

