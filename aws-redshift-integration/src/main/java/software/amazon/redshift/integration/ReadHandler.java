package software.amazon.redshift.integration;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.DescribeIntegrationsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeTagsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeTagsResponse;
import software.amazon.awssdk.services.redshift.model.Integration;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.proxy.delay.Constant;

import java.time.Duration;

import static software.amazon.redshift.integration.ErrorUtil.handleIntegrationException;

public class ReadHandler extends BaseHandlerStd {
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<RedshiftClient> proxyClient,
        final Logger logger) {
        return proxy.initiate("AWS-Redshift-Integration::Read", proxyClient, request.getDesiredResourceState(), callbackContext)
                .translateToServiceRequest(ReadHandler::describeIntegrationsRequest)
                .backoffDelay(Constant.of()
                        .delay(Duration.ofMinutes(1))
                        .timeout(Duration.ofMinutes(2))
                        .build())
                .makeServiceCall((describeIntegrationsRequest, proxyInvocation) -> proxyInvocation.injectCredentialsAndInvokeV2(describeIntegrationsRequest, proxyInvocation.client()::describeIntegrations))
                .handleError((describeIntegrationsRequest, exception, client, resourceModel, ctx) -> {
                    // it's a little strange that IntegrationConflictOperationException is thrown instead of AlreadyExists exception
                    // we need to override the default error handling because in this case we need to tell CFN that it's an AlreadyExists.
                    return handleIntegrationException(exception);
                })
                .done((describeIntegrationsRequest, describeIntegrationsResponse, proxyInvocation, model, context) -> {
                    final Integration integration = describeIntegrationsResponse.integrations().stream().findFirst().get();
                    // it's possible the model does not have the ARN populated yet,
                    // so we can just be conservative and populate it at all times.
                    model.setIntegrationArn(integration.integrationArn());

                    /**
                     * we actually don't need the describeTags call here,
                     * b/c describeIntegrations contains the tags,
                     * however, CFN requires that the READ handler needs to have read tags permission,
                     * otherwise READ handler should not return any tags.
                     */
                    if (integration.hasTags() && !integration.tags().isEmpty()) {
                        // Prepare the describeTags request
                        DescribeTagsRequest describeTagsRequest = DescribeTagsRequest.builder()
                                .resourceName(integration.integrationArn())
                                .build();

                        try {
                            // Make the describeTags call
                            DescribeTagsResponse describeTagsResponse = proxyInvocation.injectCredentialsAndInvokeV2(
                                    describeTagsRequest,
                                    proxyInvocation.client()::describeTags
                            );
                        } catch (Exception e) {
                            // Handle any errors from the describeTags call
                            return handleIntegrationException(e);
                        }
                    }

                    return ProgressEvent.success(translateToModel(integration), context);
                });
    }

    public static DescribeIntegrationsRequest describeIntegrationsRequest(final ResourceModel model) {
        if (model.getIntegrationArn() == null) {
            throw new RuntimeException("The integration model has no ARN: " + model);
        }

        return DescribeIntegrationsRequest.builder()
                .integrationArn(model.getIntegrationArn())
                .build();
    }
}
