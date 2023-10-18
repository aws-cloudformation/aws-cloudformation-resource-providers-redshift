package software.amazon.redshift.resourcepolicy;

import software.amazon.awssdk.services.cloudwatch.model.InvalidParameterValueException;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.GetResourcePolicyRequest;
import software.amazon.awssdk.services.redshift.model.GetResourcePolicyResponse;
import software.amazon.awssdk.services.redshift.model.InvalidPolicyException;
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

public class ReadHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<RedshiftClient> proxyClient,
            final Logger logger) {

        this.logger = logger;

        return proxy.initiate("AWS-Redshift-ResourcePolicy::Read", proxyClient, request.getDesiredResourceState(), callbackContext)
                .translateToServiceRequest(Translator::translateToReadRequest)
                .makeServiceCall(this::getResourcePolicy)
                .done(getResponse -> ProgressEvent.defaultSuccessHandler(Translator.translateFromReadResponse(getResponse)));
    }

    private GetResourcePolicyResponse getResourcePolicy(
            final GetResourcePolicyRequest getRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        GetResourcePolicyResponse getResponse = null;

        try {
            getResponse = proxyClient.injectCredentialsAndInvokeV2(
                    getRequest, proxyClient.client()::getResourcePolicy);
        } catch (ResourceNotFoundException e){
            throw new CfnNotFoundException(e);
        } catch (InvalidPolicyException | UnsupportedOperationException e) {
            throw new CfnInvalidRequestException(ResourceModel.TYPE_NAME, e);
        } catch (RedshiftException e) {
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        }
        logger.log(String.format("%s has successfully been read.", ResourceModel.TYPE_NAME));
        return getResponse;
    }
}
