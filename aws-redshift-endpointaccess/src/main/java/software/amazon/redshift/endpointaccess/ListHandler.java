package software.amazon.redshift.endpointaccess;

import software.amazon.awssdk.services.redshift.model.DescribeEndpointAccessResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class ListHandler extends BaseHandler<CallbackContext> {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        DescribeEndpointAccessResponse response;

        try {
            response = proxy.injectCredentialsAndInvokeV2(
                    Translator.translateToListRequest(request.getNextToken()),
                    ClientBuilder.getClient()::describeEndpointAccess
            );
        } catch (Exception e) {
            throw new CfnGeneralServiceException(ResourceModel.TYPE_NAME, e);
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModels(Translator.translateFromListRequest(response))
            .nextToken(response.marker())
            .status(OperationStatus.SUCCESS)
            .build();
    }
}
