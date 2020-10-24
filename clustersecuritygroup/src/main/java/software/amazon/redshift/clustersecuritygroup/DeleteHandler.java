package software.amazon.redshift.clustersecuritygroup;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterSecurityGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.DeleteClusterSecurityGroupResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
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
                proxy.initiate("AWS-Redshift-ClusterSecurityGroup::Delete", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                    .translateToServiceRequest(Translator::translateToDeleteRequest)
                    .makeServiceCall((awsRequest, client) -> {
                        DeleteClusterSecurityGroupResponse awsResponse;
                        try {
                            awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::deleteClusterSecurityGroup);
                        }catch (final ClusterSecurityGroupNotFoundException e) {
                            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, awsRequest.clusterSecurityGroupName());
                        }  catch (final AwsServiceException e) {
                            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
                        }
                        logger.log(String.format("%s successfully deleted.", ResourceModel.TYPE_NAME));
                        return awsResponse;
                    })
                    .progress()
            )
            .then(progress -> ProgressEvent.defaultSuccessHandler(null));
    }
}
