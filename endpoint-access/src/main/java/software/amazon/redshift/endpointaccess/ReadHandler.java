package software.amazon.redshift.endpointaccess;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAccessResponse;
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
                .then(progress -> proxy.initiate("AWS-Redshift-EndpointAccess::DescribeEndpointAccess",
                        proxyClient,
                        progress.getResourceModel(),
                        progress.getCallbackContext())
                        .translateToServiceRequest(Translator::translateToReadRequest)
                        .makeServiceCall(this::readEndPoint)
                        .done(this::constructResourceModelFromResponse)
                );
    }

    private DescribeEndpointAccessResponse readEndPoint(
            final DescribeEndpointAccessRequest request,
            final ProxyClient<RedshiftClient> proxyClient) {
        DescribeEndpointAccessResponse response = null;
        // TODO catch and throw more detailed errors
        try {
            response = proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::describeEndpointAccess);
        } catch (Exception e) {
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        }
        return response;
    }

    private ProgressEvent<ResourceModel, CallbackContext> constructResourceModelFromResponse(
            final DescribeEndpointAccessResponse response) {
        return ProgressEvent.defaultSuccessHandler(Translator.translateFromReadResponse(response));
    }
}
