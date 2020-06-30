package com.bloomreach.xm.manager.api;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public interface ListItem {

    public String getId();

    public String getName();

    @JsonFormat
            (shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ss")
    public Date getLastModified();

    public Long getSize();

    public Type getType();

    public String getLink();

    public String getPath();

    public String getHumanReadableSize();
}
