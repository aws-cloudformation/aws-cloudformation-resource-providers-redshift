package software.amazon.redshift.scheduledaction;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.redshift.model.DescribeScheduledActionsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeScheduledActionsResponse;
import software.amazon.awssdk.services.redshift.model.ScheduledActionNotFoundException;
import software.amazon.awssdk.services.redshift.model.UnauthorizedOperationException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidCredentialsException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.List;

public class ListHandler extends BaseHandler<CallbackContext> {
    private Logger logger;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {

        this.logger = logger;
        DescribeScheduledActionsRequest awsRequest = Translator.translateToListRequest(request.getNextToken());
        DescribeScheduledActionsResponse awsResponse = listScheduledActions(awsRequest, proxy);
        List<ResourceModel> models = Translator.translateFromListRequest(awsResponse);

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(models)
                .nextToken(awsResponse.marker())
                .status(OperationStatus.SUCCESS)
                .build();
    }

    private DescribeScheduledActionsResponse listScheduledActions(final DescribeScheduledActionsRequest awsRequest,
                                                                  final AmazonWebServicesClientProxy proxy) {
        DescribeScheduledActionsResponse awsResponse;
        try {
            awsResponse = proxy.injectCredentialsAndInvokeV2(awsRequest, ClientBuilder.getClient()::describeScheduledActions);
        } catch (final ScheduledActionNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, awsRequest.scheduledActionName(), e);
        } catch (final UnauthorizedOperationException e) {
            throw new CfnInvalidCredentialsException(e);
        } catch (final AwsServiceException e) {
            throw new CfnGeneralServiceException(e);
        }

        logger.log(String.format("%s has successfully been listed.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }
}
