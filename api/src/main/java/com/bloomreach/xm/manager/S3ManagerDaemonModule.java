package com.bloomreach.xm.manager;

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import org.onehippo.repository.jaxrs.CXFRepositoryJaxrsEndpoint;
import org.onehippo.repository.jaxrs.RepositoryJaxrsService;
import org.onehippo.repository.jaxrs.api.ManagedUserSessionInvoker;
import org.onehippo.repository.modules.AbstractReconfigurableDaemonModule;

import com.bloomreach.xm.manager.s3.controller.AwsS3ProxyController;
import com.bloomreach.xm.manager.s3.service.AwsCredentials;
import com.bloomreach.xm.manager.s3.service.AwsS3Service;
import com.bloomreach.xm.manager.s3.service.AwsService;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

public class S3ManagerDaemonModule extends AbstractReconfigurableDaemonModule {

    private static final String END_POINT = "/s3manager";
    private String accessKey;
    private String secretKey;
    private String bucket;

    @Override
    protected void doConfigure(final Node moduleConfig) throws RepositoryException {
        accessKey = moduleConfig.getProperty("accessKey").getString();
        secretKey = moduleConfig.getProperty("secretKey").getString();
        bucket = moduleConfig.getProperty("bucket").getString();
    }

    @Override
    public void doInitialize(final Session session) throws RepositoryException {
        AwsCredentials awsCredentials = new AwsCredentials(accessKey, secretKey);
        AwsService awsService = new AwsService(awsCredentials);
        AwsS3Service awsS3Service = new AwsS3Service(awsService, bucket);
        AwsS3ProxyController awsS3ProxyController = new AwsS3ProxyController(awsS3Service, session);

        ManagedUserSessionInvoker managedUserSessionInvoker = new ManagedUserSessionInvoker(session);

        RepositoryJaxrsService.addEndpoint(new CXFRepositoryJaxrsEndpoint(END_POINT)
                .invoker(managedUserSessionInvoker)
                .singleton(awsS3ProxyController)
                .singleton(new JacksonJsonProvider())
        );
    }

    @Override
    protected void doShutdown() {
        RepositoryJaxrsService.removeEndpoint(END_POINT);
    }

    @Override
    protected void onConfigurationChange(final Node moduleConfig) throws RepositoryException {
        doConfigure(moduleConfig);
    }

}