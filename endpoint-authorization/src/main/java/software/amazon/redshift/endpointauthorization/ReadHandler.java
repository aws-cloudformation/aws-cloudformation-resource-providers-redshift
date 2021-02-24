package software.amazon.redshift.endpointauthorization;

import com.google.common.annotations.VisibleForTesting;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAuthorizationRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAuthorizationResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
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

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress -> proxy.initiate(
                        "AWS-Redshift-EndpointAuthorization::Read",
                        proxyClient,
                        progress.getResourceModel(),
                        progress.getCallbackContext())
                        .translateToServiceRequest(Translator::translateToReadRequest)
                        .makeServiceCall(this::readEndpointAuthorization)
                        .done(this::constructResourceModelFromResponse)
                );

    }

    @VisibleForTesting
    DescribeEndpointAuthorizationResponse readEndpointAuthorization(
            final DescribeEndpointAuthorizationRequest request,
            final ProxyClient<RedshiftClient> proxyClient) {
        DescribeEndpointAuthorizationResponse response = null;
        // TODO catch and throw more detailed errors
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(
                    request, proxyClient.client()::describeEndpointAuthorization
            );
        } catch (Exception e) {
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        }
        return response;
    }

    @VisibleForTesting
    ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(
            final DescribeEndpointAuthorizationResponse response) {
        return ProgressEvent.defaultSuccessHandler(Translator.translateFromReadResponse(response));
    }
}
