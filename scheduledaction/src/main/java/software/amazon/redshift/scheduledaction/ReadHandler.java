package software.amazon.redshift.scheduledaction;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.DescribeScheduledActionsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeScheduledActionsResponse;
import software.amazon.awssdk.services.redshift.model.ScheduledActionNotFoundException;
import software.amazon.awssdk.services.redshift.model.UnauthorizedOperationException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
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

        return proxy.initiate("AWS-Redshift-ScheduledAction::Read", proxyClient, request.getDesiredResourceState(), callbackContext)
                .translateToServiceRequest(Translator::translateToReadRequest)
                .makeServiceCall(this::describeScheduledActions)
                .handleError(this::describeScheduledActionsErrorHandler)
                .done(awsResponse -> ProgressEvent.defaultSuccessHandler(Translator.translateFromReadResponse(awsResponse)));
    }

    private DescribeScheduledActionsResponse describeScheduledActions(final DescribeScheduledActionsRequest awsRequest,
                                                                      final ProxyClient<RedshiftClient> proxyClient) {
        DescribeScheduledActionsResponse awsResponse;
        awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeScheduledActions);

        logger.log(String.format("%s has successfully been read.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }

    private ProgressEvent<ResourceModel, CallbackContext> describeScheduledActionsErrorHandler(final DescribeScheduledActionsRequest awsRequest,
                                                                                               final Exception exception,
                                                                                               final ProxyClient<RedshiftClient> client,
                                                                                               final ResourceModel model,
                                                                                               final CallbackContext context) {
        if (exception instanceof ScheduledActionNotFoundException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.NotFound);
        } else if (exception instanceof UnauthorizedOperationException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.InvalidCredentials);
        } else {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.GeneralServiceException);
        }
    }
}
