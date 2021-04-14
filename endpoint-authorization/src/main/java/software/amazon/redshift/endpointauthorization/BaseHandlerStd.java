package software.amazon.redshift.endpointauthorization;

import com.amazonaws.util.StringUtils;
import com.google.common.annotations.VisibleForTesting;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.AuthorizeEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAuthorizationRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAuthorizationResponse;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import javax.annotation.Nullable;

// Placeholder for the functionality that could be shared across Create/Read/Update/Delete/List Handlers

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {
    @Override
    public final ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {
        return handleRequest(
                proxy,
                request,
                callbackContext != null ? callbackContext : new CallbackContext(),
                proxy.newProxy(ClientBuilder::getClient),
                logger
        );
    }

    protected abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<RedshiftClient> proxyClient,
            final Logger logger);


    protected void validateAuthNotExists(final AuthorizeEndpointAccessRequest request,
                               final ProxyClient<RedshiftClient> proxyClient) {
        // The API will not throw an easily parsable error if the endpoint auth already exists.
        // Here we will do a manual check and throw the CfnAlreadyExistsError.

        DescribeEndpointAuthorizationRequest describeRequest = DescribeEndpointAuthorizationRequest.builder()
                .account(request.account())
                .clusterIdentifier(request.clusterIdentifier())
                .build();

        try {
            DescribeEndpointAuthorizationResponse describeResponse = proxyClient.injectCredentialsAndInvokeV2(
                    describeRequest, proxyClient.client()::describeEndpointAuthorization
            );

            if (!describeResponse.endpointAuthorizationList().isEmpty()) {
                throw new CfnAlreadyExistsException(
                        ResourceModel.TYPE_NAME,
                        String.format("account:%s-clusteridentifier:%s",
                                request.account(), request.clusterIdentifier())
                );
            }
        } catch (Exception e) {
            // If anything happened, we can just return false (does not exist). The error checking for cluster id
            // etc should be at the create level.
        }
    }

    protected void validateAuthExists(final AuthorizeEndpointAccessRequest request,
                                      final ProxyClient<RedshiftClient> proxyClient) {
        // The API will not throw an easily parsable error if the endpoint auth already exists.
        // Here we will do a manual check and throw the CfnAlreadyExistsError.

        DescribeEndpointAuthorizationRequest describeRequest = DescribeEndpointAuthorizationRequest.builder()
                .account(request.account())
                .clusterIdentifier(request.clusterIdentifier())
                .build();
        DescribeEndpointAuthorizationResponse describeResponse = null;
        try {
             describeResponse = proxyClient.injectCredentialsAndInvokeV2(
                    describeRequest, proxyClient.client()::describeEndpointAuthorization
            );

        } catch (Exception e) {
            // If anything happened, we can just return false (does not exist). The error checking for cluster id
            // etc should be at the create level.
        }

        if (describeResponse != null && describeResponse.endpointAuthorizationList().isEmpty()) {
            throw new CfnNotFoundException(
                    ResourceModel.TYPE_NAME,
                    String.format("account:%s-clusteridentifier:%s",
                            request.account(), request.clusterIdentifier())
            );
        }
    }


    boolean doesNotExist(@Nullable String string) {
        return StringUtils.isNullOrEmpty(string);
    }

    @VisibleForTesting
    DeleteHandler getDeleteHandler() {
        return new DeleteHandler();
    }

    @VisibleForTesting
    CreateHandler getCreateHandler() {
        return new CreateHandler();
    }

    @VisibleForTesting
    ReadHandler getReadHandler() {
        return new ReadHandler();
    }

}
