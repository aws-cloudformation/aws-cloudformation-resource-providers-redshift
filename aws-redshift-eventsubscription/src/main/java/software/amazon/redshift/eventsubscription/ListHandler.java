package software.amazon.redshift.eventsubscription;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.redshift.model.DescribeEventSubscriptionsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEventSubscriptionsResponse;
import software.amazon.awssdk.services.redshift.model.InvalidTagException;
import software.amazon.awssdk.services.redshift.model.SubscriptionNotFoundException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
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
        DescribeEventSubscriptionsRequest awsRequest = Translator.translateToListRequest(request.getNextToken());
        DescribeEventSubscriptionsResponse awsResponse = listEventSubscriptions(awsRequest, proxy);
        List<ResourceModel> models = Translator.translateFromListRequest(awsResponse);

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModels(models)
                .nextToken(awsResponse.marker())
                .status(OperationStatus.SUCCESS)
                .build();
    }

    private DescribeEventSubscriptionsResponse listEventSubscriptions(final DescribeEventSubscriptionsRequest awsRequest,
                                                                      final AmazonWebServicesClientProxy proxy) {
        DescribeEventSubscriptionsResponse awsResponse;
        try {
            awsResponse = proxy.injectCredentialsAndInvokeV2(awsRequest, ClientBuilder.getClient()::describeEventSubscriptions);
        } catch (final SubscriptionNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, awsRequest.subscriptionName(), e);
        } catch (final InvalidTagException e) {
            throw new CfnInvalidRequestException(e);
        } catch (final AwsServiceException e) {
            throw new CfnGeneralServiceException(e);
        }

        logger.log(String.format("%s has successfully been listed.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }
}
