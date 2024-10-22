package software.amazon.redshift.integration;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.DescribeIntegrationsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeIntegrationsResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.stream.Collectors;

import static software.amazon.redshift.integration.ErrorUtil.handleIntegrationException;

public class ListHandler extends BaseHandlerStd {

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<RedshiftClient> proxyClient,
        final Logger logger) {
        try {
            DescribeIntegrationsResponse describeIntegrationsResponse = proxy.injectCredentialsAndInvokeV2(
                    describeIntegrationsRequest(request.getNextToken()),
                    proxyClient.client()::describeIntegrations);

            return ProgressEvent.<ResourceModel, CallbackContext>builder()
                    .resourceModels(
                            describeIntegrationsResponse.integrations()
                                    .stream()
                                    .map(BaseHandlerStd::translateToModel)
                                    .collect(Collectors.toList())
                    ).nextToken(describeIntegrationsResponse.marker())
                    .status(OperationStatus.SUCCESS)
                    .build();
        } catch (Exception e) {
            return handleIntegrationException(e);
        }
    }

    static DescribeIntegrationsRequest describeIntegrationsRequest(final String nextToken) {
        return DescribeIntegrationsRequest.builder()
                .marker(nextToken)
                .build();
    }
}
