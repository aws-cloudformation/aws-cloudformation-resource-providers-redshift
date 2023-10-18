package software.amazon.redshift.resourcepolicy;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.PutResourcePolicyRequest;
import software.amazon.awssdk.services.redshift.model.PutResourcePolicyResponse;
import software.amazon.awssdk.services.redshift.model.RedshiftException;
import software.amazon.awssdk.services.redshift.model.ResourceNotFoundException;
import software.amazon.awssdk.services.redshift.model.InvalidPolicyException;
import software.amazon.awssdk.services.redshift.model.UnsupportedOperationException;
import software.amazon.awssdk.services.cloudwatch.model.InvalidParameterValueException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;


public class CreateHandler extends BaseHandlerStd {
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
                        proxy.initiate("AWS-Redshift-ResourcePolicy::Create", proxyClient,progress.getResourceModel(), progress.getCallbackContext())
                                .translateToServiceRequest(Translator::translateToCreateRequest)
                                .makeServiceCall(this::putResourcePolicy)
                                .progress()
                )
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private PutResourcePolicyResponse putResourcePolicy(
            final PutResourcePolicyRequest putRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        PutResourcePolicyResponse putResponse = null;

        try {
            putResponse = proxyClient.injectCredentialsAndInvokeV2(
                    putRequest, proxyClient.client()::putResourcePolicy);
        } catch (ResourceNotFoundException e){
            throw new CfnNotFoundException(e);
        } catch (InvalidPolicyException |
                 UnsupportedOperationException |
                 InvalidParameterValueException e) {
            throw new CfnInvalidRequestException(ResourceModel.TYPE_NAME, e);
        } catch (RedshiftException e) {
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        }

        logger.log(String.format("%s successfully created.", ResourceModel.TYPE_NAME));
        return putResponse;
    }

}
