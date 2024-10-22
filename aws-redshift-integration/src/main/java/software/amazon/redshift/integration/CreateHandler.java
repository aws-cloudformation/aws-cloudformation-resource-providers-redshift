package software.amazon.redshift.integration;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.redshift.model.CreateIntegrationRequest;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.cloudformation.proxy.delay.Constant;

import java.time.Duration;
import java.util.Optional;

import static software.amazon.redshift.integration.ErrorUtil.handleIntegrationException;

public class CreateHandler extends BaseHandlerStd {
    private final static IdentifierFactory integrationNameFactory = new IdentifierFactory(
            STACK_NAME,
            RESOURCE_IDENTIFIER,
            MAX_LENGTH_INTEGRATION
    );

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<RedshiftClient> proxyClient,
        final Logger logger) {
        final ResourceModel model = request.getDesiredResourceState();

        final Tagging.TagSet allTags = Tagging.TagSet.builder()
                .systemTags(Tagging.translateTagsToSdk(request.getSystemTags()))
                .stackTags(Tagging.translateTagsToSdk(request.getDesiredResourceTags()))
                .resourceTags(Tagging.translateTagsToSdk(request.getDesiredResourceState().getTags()))
                .build();

        return ProgressEvent.progress(model, callbackContext)
                .then(progress -> setIntegrationNameIfEmpty(request, progress))
                .then(progress -> createIntegration(proxy, proxyClient, progress, allTags))
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private ProgressEvent<ResourceModel, CallbackContext> createIntegration(final AmazonWebServicesClientProxy proxy,
                                                                            final ProxyClient<RedshiftClient> proxyClient,
                                                                            final ProgressEvent<ResourceModel, CallbackContext> progress,
                                                                            final Tagging.TagSet tags) {
        return proxy.initiate("Redshift::create-integration", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                .translateToServiceRequest((resourceModel) -> createIntegrationRequest(resourceModel, tags))
                .backoffDelay(Constant.of()
                        .delay(Duration.ofSeconds(30))
                        .timeout(Duration.ofMinutes(2))
                        .build())
                .makeServiceCall((createIntegrationRequest, proxyInvocation) -> proxyInvocation.injectCredentialsAndInvokeV2(createIntegrationRequest, proxyInvocation.client()::createIntegration))
                .stabilize((createIntegrationRequest, createIntegrationResponse, proxyInvocation, resourceModel, context) -> {
                    // manually add in delaying before we describe-integrations to get the status,
                    // we allow cluster WF more time to finish.
                    // with the response, now we'd know what the ARN is.
                    resourceModel.setIntegrationArn(
                            Optional.ofNullable(resourceModel.getIntegrationArn()).orElse(createIntegrationResponse.integrationArn())
                    );
                    return isStabilized(resourceModel, proxyInvocation);
                })
                .handleError((createRequest, exception, client, resourceModel, ctx) -> {
                    // it's a little strange that IntegrationConflictOperationException is thrown instead of AlreadyExists exception
                    // we need to override the default error handling because in this case we need to tell CFN that it's an AlreadyExists.
                    return handleIntegrationException(exception);
                })
                .progress();
    }

    private static CreateIntegrationRequest createIntegrationRequest(
            final ResourceModel model,
            final Tagging.TagSet tags
    ) {
        return CreateIntegrationRequest.builder()
                .kmsKeyId(model.getKMSKeyId())
                .integrationName(model.getIntegrationName())
                .sourceArn(model.getSourceArn())
                .targetArn(model.getTargetArn())
                .additionalEncryptionContext(model.getAdditionalEncryptionContext())
                .tagList(Tagging.translateTagsToSdk(tags))
                .build();
    }

    private ProgressEvent<ResourceModel, CallbackContext> setIntegrationNameIfEmpty(final ResourceHandlerRequest<ResourceModel> request,
                                                                                    final ProgressEvent<ResourceModel, CallbackContext> progress
    ) {
        ResourceModel model = progress.getResourceModel();
        if (StringUtils.isNullOrEmpty(model.getIntegrationName())) {
            model.setIntegrationName(integrationNameFactory.newIdentifier()
                    .withStackId(request.getStackId())
                    .withResourceId(request.getLogicalResourceIdentifier())
                    .withRequestToken(request.getClientRequestToken())
                    .toString());
        }
        return progress;
    }
}
