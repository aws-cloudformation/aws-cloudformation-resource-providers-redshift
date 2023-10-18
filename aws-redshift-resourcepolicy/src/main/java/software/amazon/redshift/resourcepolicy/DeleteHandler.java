package software.amazon.redshift.resourcepolicy;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.DeleteResourcePolicyRequest;
import software.amazon.awssdk.services.redshift.model.DeleteResourcePolicyResponse;
import software.amazon.awssdk.services.redshift.model.RedshiftException;
import software.amazon.awssdk.services.redshift.model.ResourceNotFoundException;
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
                        proxy.initiate("AWS-Redshift-ResourcePolicy::Delete", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                                .translateToServiceRequest(Translator::translateToDeleteRequest)
                                .makeServiceCall(this::deleteResourcePolicy)
                                .progress()
                )

                .then(progress -> ProgressEvent.defaultSuccessHandler(null));
    }

    DeleteResourcePolicyResponse deleteResourcePolicy(
            final DeleteResourcePolicyRequest deleteRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        DeleteResourcePolicyResponse deleteResponse = null;
        try {
            deleteResponse = proxyClient.injectCredentialsAndInvokeV2(
                    deleteRequest, proxyClient.client()::deleteResourcePolicy);
        } catch (ResourceNotFoundException e){
            throw new CfnNotFoundException(e);
        } catch (UnsupportedOperationException e) {
            throw new CfnInvalidRequestException(ResourceModel.TYPE_NAME, e);
        } catch (RedshiftException e) {
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        }

        logger.log(String.format("%s successfully deleted.", ResourceModel.TYPE_NAME));
        return deleteResponse;
    }
}
