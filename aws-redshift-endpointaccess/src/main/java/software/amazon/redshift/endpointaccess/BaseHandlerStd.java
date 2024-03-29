package software.amazon.redshift.endpointaccess;

import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

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
            final Logger logger
    );

    protected boolean hasReadOnlyProperties(ResourceModel resourceModel) {
        /*
        Read only properties:
            - Address
            - EndpointStatus
            - EndpointCreateTime
            - Port
            - VpcSecurityGroups
            - VpcEndpoint
         */

        return resourceModel.getAddress() != null
                || resourceModel.getEndpointStatus() != null
                || resourceModel.getEndpointCreateTime() != null
                || resourceModel.getPort() != null
                || resourceModel.getVpcSecurityGroups() != null
                || resourceModel.getVpcEndpoint() != null;

    }

    protected void logResourceModelRequest(ResourceModel resourceModel, Logger logger) {
        logger.log("Received resource model: " + resourceModel);
    }

    protected void logAPICall(AwsRequest awsRequest, String apiName, Logger logger) {
        logger.log(String.format("Sending request %s to API %s", awsRequest, apiName));
    }
}
