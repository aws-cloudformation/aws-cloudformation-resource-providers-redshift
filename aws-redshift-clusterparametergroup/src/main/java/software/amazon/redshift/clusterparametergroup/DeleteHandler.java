package software.amazon.redshift.clusterparametergroup;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterParameterGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.DeleteClusterParameterGroupRequest;
import software.amazon.awssdk.services.redshift.model.DeleteClusterParameterGroupResponse;
import software.amazon.awssdk.services.redshift.model.InvalidClusterParameterGroupStateException;
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
                                .makeServiceCall(this::deleteClusterParameterGroup)
                                .handleError(this::deleteClusterParameterGroupErrorHandler)
                                .progress()
                )

                .then(progress -> ProgressEvent.defaultSuccessHandler(null));
    }

    private DeleteClusterParameterGroupResponse deleteClusterParameterGroup(final DeleteClusterParameterGroupRequest awsRequest,
                                                                            final ProxyClient<RedshiftClient> proxyClient) {
        DeleteClusterParameterGroupResponse awsResponse;
        awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::deleteClusterParameterGroup);

        logger.log(String.format("%s successfully deleted.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }

    private ProgressEvent<ResourceModel, CallbackContext> deleteClusterParameterGroupErrorHandler(final DeleteClusterParameterGroupRequest awsRequest,
                                                                                                  final Exception exception,
                                                                                                  final ProxyClient<RedshiftClient> client,
                                                                                                  final ResourceModel model,
                                                                                                  final CallbackContext context) {
        if (exception instanceof ClusterParameterGroupNotFoundException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.NotFound);

        } else if (exception instanceof InvalidClusterParameterGroupStateException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.ResourceConflict);

        } else {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.GeneralServiceException);
        }
    }
}
