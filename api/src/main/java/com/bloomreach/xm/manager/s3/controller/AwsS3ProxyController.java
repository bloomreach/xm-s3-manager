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
package com.bloomreach.xm.manager.s3.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;
import org.hippoecm.repository.api.HippoSession;
import org.onehippo.cms7.services.cmscontext.CmsSessionContext;
import org.onehippo.cms7.utilities.servlet.HttpSessionBoundJcrSessionHolder;
import org.onehippo.repository.security.SessionUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bloomreach.xm.manager.api.ProxyController;
import com.bloomreach.xm.manager.s3.model.DZConfiguration;
import com.bloomreach.xm.manager.s3.model.S3ListItem;
import com.bloomreach.xm.manager.s3.service.AwsS3ServiceImpl;
import com.bloomreach.xm.manager.s3.model.S3Permissions;

@Path("/awsS3")
public class AwsS3ProxyController implements ProxyController<S3ListItem> {

    private static final Logger log = LoggerFactory.getLogger(AwsS3ProxyController.class);
    public static final String S3_USER_PERMISSION = "xm.s3manager.user";
    public static final String S3_UPLOAD_PERMISSION = "xm.s3manager-upload.user";
    public static final String S3_DELETE_PERMISSION = "xm.s3manager-delete.user";
    public static final String S3_CREATE_PERMISSION = "xm.s3manager-create.user";
    private final AwsS3ServiceImpl awsS3Service;
    private final Session systemSession;
    private static DZConfiguration dzConfiguration;

    public AwsS3ProxyController(final AwsS3ServiceImpl awsS3Service, final Session systemSession, final DZConfiguration dzConfiguration) {
        this.awsS3Service = awsS3Service;
        this.systemSession =systemSession;
        AwsS3ProxyController.dzConfiguration = dzConfiguration;
    }

    @Path("/deleteFiles")
    @DELETE
    public void deleteFiles(@Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse, List<S3ListItem> items) throws IOException {
        S3Permissions s3Permissions = getUserPermissions(httpServletRequest);
        if(s3Permissions.isDeleteAllowed()) {
            awsS3Service.deleteFiles(items);
            log.info("User: {} deleted the files: {}",
                    getUserFromSession(httpServletRequest).getId(),
                    items.stream().map(S3ListItem::getPath).toArray(String[]::new)
            );
        } else {
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }


    @Path("/createFolder")
    @POST
    public void createFolder(@Context HttpServletRequest httpServletRequest, @Context HttpServletResponse httpServletResponse, Map<String, String> variables) throws IOException {
        S3Permissions s3Permissions = getUserPermissions(httpServletRequest);
        if(s3Permissions.isCreateAllowed()) {
            awsS3Service.createFolder(variables.get("path"));
            log.info("User: {} created a directory with path: {}",
                    getUserFromSession(httpServletRequest).getId(),
                    variables.get("path")
            );
        } else {
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    @Path("/clear")
    @POST
    public void clear(Map<String, String> variables) {
        final String uniqueFileName = variables.get("path") + variables.get("filename");
        awsS3Service.abortMultipartUploadAndClear(uniqueFileName);
    }

    @Path("/uploadFiles")
    @POST
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    public void uploadFile(@Context HttpServletRequest httpServletRequest,
                           @Context HttpServletResponse httpServletResponse,
                           @Multipart(value = "file", required = false) Attachment file,
                           @Multipart(value = "dzchunkindex", required = false) String index,
                           @Multipart(value = "dztotalchunkcount", required = false) String total,
                           @QueryParam(value = "path") String path) throws IOException {
        S3Permissions s3Permissions = getUserPermissions(httpServletRequest);
        if(s3Permissions.isUploadAllowed()) {
            SessionUser user = getUserFromSession(httpServletRequest);
            if (total != null) {
                awsS3Service.uploadMultipart(user, file, path, Integer.parseInt(index), Integer.parseInt(total));
                log.info("User: {} uploaded multipart file: {} to path: {}",
                        getUserFromSession(httpServletRequest).getId(),
                        file.getHeader("Content-Disposition")
                                .substring(file.getHeader("Content-Disposition")
                                        .lastIndexOf("=")+1).replace("\"",""),
                        path.isEmpty()? "/" : path
                );
            } else {
                awsS3Service.uploadSinglepart(user, file, path);
                log.info("User: {} uploaded file: {} to path: {}",
                        getUserFromSession(httpServletRequest).getId(),
                        file.getHeader("Content-Disposition")
                                .substring(file.getHeader("Content-Disposition")
                                        .lastIndexOf("=")+1).replace("\"",""),
                        path.isEmpty()? "/" : path
                );
            }
        } else {
            httpServletResponse.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }

    @Path("/list")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public List<S3ListItem> list(@DefaultValue (value = "") @QueryParam(value = "path") String path, @QueryParam(value = "query") String query) {
        return this.awsS3Service.getList(path, query);
    }

    @Path("/acl")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public S3Permissions hasPermission(@Context HttpServletRequest httpServletRequest){
        return getUserPermissions(httpServletRequest);
    }

    private S3Permissions getUserPermissions(final HttpServletRequest httpServletRequest){
        final Session userSession = getUserSession(httpServletRequest);
        return userSession!=null ? new S3Permissions(
                ((HippoSession)userSession).isUserInRole(S3_USER_PERMISSION),
                ((HippoSession)userSession).isUserInRole(S3_UPLOAD_PERMISSION),
                ((HippoSession)userSession).isUserInRole(S3_DELETE_PERMISSION),
                ((HippoSession)userSession).isUserInRole(S3_CREATE_PERMISSION)
        ) : new S3Permissions(false, false, false, false);
    }

    private SessionUser getUserFromSession(final HttpServletRequest httpServletRequest){
        try {
            HippoSession hippoSession = (HippoSession) getUserSession(httpServletRequest);
            return hippoSession!=null ? hippoSession.getUser() : null;
        } catch (ItemNotFoundException e) {
            log.error("An exception occurred while trying to retrieve user from session.", e);
        }
        return null;
    }

    private Session getUserSession(final HttpServletRequest httpServletRequest) {
        final HttpSession httpSession = httpServletRequest.getSession(false);
        final CmsSessionContext cmsSessionContext = CmsSessionContext.getContext(httpSession);
        final SimpleCredentials credentials = cmsSessionContext.getRepositoryCredentials();
        try {
            return HttpSessionBoundJcrSessionHolder.getOrCreateJcrSession(AwsS3ProxyController.class.getName() + ".session",
                    httpSession, credentials, systemSession.getRepository()::login);
        } catch (RepositoryException e){
            log.error("Error while trying to retrieve user session.", e);
        }
        return null;
    }

    @Path("/dzconf")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public DZConfiguration getDZConfiguration(){
        return dzConfiguration;
    }
}
