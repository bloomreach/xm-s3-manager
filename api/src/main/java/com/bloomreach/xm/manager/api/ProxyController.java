package com.bloomreach.xm.manager.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import com.bloomreach.xm.manager.common.api.ListItem;
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
                    @Multipart(value = "chunkFile", required = false) Attachment chunkFile,
                    @Multipart(value = "UploadFiles", required = false) Attachment uploadFiles,
                    @Multipart(value = "chunkIndex", required = false) Integer index,
                    @Multipart(value = "totalChunk", required = false) Integer total,
                    @QueryParam(value = "path") String path) throws IOException;

    @Path("/list")
    @GET
    List<T> list(@DefaultValue(value = "") @QueryParam(value = "path") String path, @QueryParam(value = "query") String query);

    @Path("/acl")
    @GET
    S3Permissions hasPermission(@Context HttpServletRequest httpServletRequest);
}
