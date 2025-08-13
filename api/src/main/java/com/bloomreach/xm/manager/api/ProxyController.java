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
package com.bloomreach.xm.manager.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import com.bloomreach.xm.manager.s3.model.S3Permissions;

public interface ProxyController<T extends ListItem> {

    @Path("/deleteFiles")
    @DELETE
    void deleteFiles(@Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse, List<T> items) throws IOException;


    @Path("/createFolder")
    @POST
    void createFolder(@Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse, Map<String, String> variables) throws IOException;


    @Path("/uploadFiles")
    @POST
    void uploadFile(@Context HttpServletRequest httpServletRequest,
                    @Context HttpServletResponse httpServletResponse,
                    @Multipart(value = "file", required = false) Attachment file,
                    @Multipart(value = "dzchunkindex", required = false) String index,
                    @Multipart(value = "dztotalchunkcount", required = false) String total,
                    @QueryParam(value = "path") String path) throws IOException;

    @Path("/list")
    @GET
    List<T> list(@DefaultValue(value = "") @QueryParam(value = "path") String path, @QueryParam(value = "query") String query);

    @Path("/acl")
    @GET
    S3Permissions hasPermission(@Context HttpServletRequest httpServletRequest);
}
