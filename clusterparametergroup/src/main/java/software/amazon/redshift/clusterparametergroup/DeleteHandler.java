package software.amazon.redshift.clusterparametergroup;

import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.*;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<RedshiftClient> proxyClient,
            final Logger logger) {

        this.logger = logger;

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress ->
                        proxy.initiate("AWS-Redshift-ClusterParameterGroup::Delete", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                                .translateToServiceRequest(Translator::translateToDeleteRequest)
                                .makeServiceCall((awsRequest, client) -> {
                                    DeleteClusterParameterGroupResponse awsResponse;
                                    try {
                                        awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::deleteClusterParameterGroup);
                                        logger.log(String.format("%s [%s] Deleted Successfully", ResourceModel.TYPE_NAME, awsRequest.parameterGroupName()));
                                    } catch (final ClusterParameterGroupNotFoundException e) {
                                        throw new CfnNotFoundException(ResourceModel.TYPE_NAME, awsRequest.parameterGroupName());
                                    } catch (final InvalidClusterParameterGroupStateException e) {
                                        throw new CfnInvalidRequestException(awsRequest.toString(), e);
                                    }
                                    logger.log(String.format("%s successfully deleted.", ResourceModel.TYPE_NAME));
                                    return awsResponse;
                                }).success());
    }
}
