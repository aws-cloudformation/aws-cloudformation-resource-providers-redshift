package software.amazon.redshift.clustersubnetgroup;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.DeleteClusterSubnetGroupRequest;
import software.amazon.awssdk.services.redshift.model.DeleteClusterSubnetGroupResponse;
import software.amazon.awssdk.services.redshift.model.InvalidClusterSubnetGroupStateException;
import software.amazon.awssdk.services.redshift.model.InvalidClusterSubnetStateException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<RedshiftClient> proxyClient,
            final Logger logger) {

        this.logger = logger;

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress ->
                        proxy.initiate(String.format("%s::Delete", CALL_GRAPH_TYPE_NAME), proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                                .translateToServiceRequest(Translator::translateToDeleteRequest)
                                .makeServiceCall(this::deleteClusterSubnetGroup)
                                .handleError(this::deleteClusterSubnetGroupErrorHandler)
                                .progress()
                )
                .then(progress -> ProgressEvent.defaultSuccessHandler(null));
    }

    private DeleteClusterSubnetGroupResponse deleteClusterSubnetGroup(
            final DeleteClusterSubnetGroupRequest awsRequest,
            final ProxyClient<RedshiftClient> proxyClient) {

        DeleteClusterSubnetGroupResponse awsResponse = proxyClient.injectCredentialsAndInvokeV2(
                awsRequest,
                proxyClient.client()::deleteClusterSubnetGroup);

        logger.log(String.format("%s successfully deleted.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }

    private ProgressEvent<ResourceModel, CallbackContext> deleteClusterSubnetGroupErrorHandler(
            final DeleteClusterSubnetGroupRequest awsRequest,
            final Exception exception,
            final ProxyClient<RedshiftClient> client,
            final ResourceModel model,
            final CallbackContext context) {

        if (exception instanceof ClusterSubnetGroupNotFoundException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.NotFound);
        } else if (exception instanceof InvalidClusterSubnetGroupStateException ||
                exception instanceof InvalidClusterSubnetStateException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.ResourceConflict);
        } else {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.GeneralServiceException);
        }
    }
}