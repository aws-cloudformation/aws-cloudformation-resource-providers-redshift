package software.amazon.redshift.integration;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ModifyIntegrationRequest;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Collection;
import java.util.HashSet;

import static software.amazon.redshift.integration.ErrorUtil.handleIntegrationException;
import static software.amazon.redshift.integration.Tagging.translateTagsToSdk;
import static software.amazon.redshift.integration.Translator.shouldModifyField;

public class UpdateHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<RedshiftClient> proxyClient,
            final Logger logger) {
        // Currently Integration resource only supports Tags update.
        final ResourceModel desiredModel = request.getDesiredResourceState();
        final ResourceModel previousModel = request.getPreviousResourceState();

        final Tagging.TagSet previousTags = Tagging.TagSet.builder()
                .systemTags(translateTagsToSdk(request.getPreviousSystemTags()))
                .stackTags(translateTagsToSdk(request.getPreviousResourceTags()))
                .resourceTags(new HashSet<>(translateTagsToSdk(request.getPreviousResourceState().getTags())))
                .build();

        final Tagging.TagSet desiredTags = Tagging.TagSet.builder()
                .systemTags(translateTagsToSdk(request.getSystemTags()))
                .stackTags(translateTagsToSdk(request.getDesiredResourceTags()))
                .resourceTags(new HashSet<>(translateTagsToSdk(request.getDesiredResourceState().getTags())))
                .build();

        return ProgressEvent.progress(desiredModel, callbackContext)
                .then(progress -> updateTags(proxy, proxyClient, progress, previousTags, desiredTags))
                .then(progress -> {
                    if (shouldModifyIntegration(previousModel, desiredModel)) {
                        return modifyIntegration(proxy, proxyClient, previousModel, progress);
                    }
                    return progress;
                })
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private boolean shouldModifyIntegration(final ResourceModel previousModel, final ResourceModel desiredModel) {
        return previousModel != null && (
                        shouldModifyField(previousModel, desiredModel, ResourceModel::getIntegrationName));
    }

    private ProgressEvent<ResourceModel, CallbackContext> modifyIntegration(final AmazonWebServicesClientProxy proxy,
                                                                            final ProxyClient<RedshiftClient> proxyClient,
                                                                            final ResourceModel previousModel,
                                                                            final ProgressEvent<ResourceModel, CallbackContext> progress) {
        return proxy.initiate("rds::modify-integration", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                .translateToServiceRequest((desiredModel) ->
                        modifyIntegrationRequest(previousModel, desiredModel))
                .backoffDelay(config.getBackoff())
                .makeServiceCall((modifyIntegrationRequest, proxyInvocation) -> proxyInvocation
                        .injectCredentialsAndInvokeV2(modifyIntegrationRequest, proxyInvocation.client()::modifyIntegration))
                .stabilize((modifyIntegrationRequest, modifyIntegrationResponse, proxyInvocation, resourceModel, context) -> isStabilized(resourceModel, proxyInvocation))
                .handleError((createRequest, exception, client, resourceModel, ctx) -> {
                    // it's a little strange that IntegrationConflictOperationException is thrown instead of AlreadyExists exception
                    // we need to override the default error handling because in this case we need to tell CFN that it's an AlreadyExists.
                    return handleIntegrationException(exception);
                })
                .progress();
    }

    public static ModifyIntegrationRequest modifyIntegrationRequest(
            final ResourceModel previousModel,
            final ResourceModel desiredModel
    ) {
        ModifyIntegrationRequest.Builder builder = ModifyIntegrationRequest.builder()
                .integrationArn(desiredModel.getIntegrationArn());

        if (shouldModifyField(previousModel, desiredModel, ResourceModel::getIntegrationName)) {
            // integration name can not be empty here, because we will populate it at the model level.
            builder.integrationName(desiredModel.getIntegrationName());
        }

        return builder.build();
    }

    protected ProgressEvent<ResourceModel, CallbackContext> updateTags(
            final AmazonWebServicesClientProxy proxy,
            final ProxyClient<RedshiftClient> RedshiftClientProxyClient,
            final ProgressEvent<ResourceModel, CallbackContext> progress,
            final Tagging.TagSet previousTags,
            final Tagging.TagSet desiredTags) {
        final Collection<software.amazon.awssdk.services.redshift.model.Tag> effectivePreviousTags = Tagging.translateTagsToSdk(previousTags);
        final Collection<software.amazon.awssdk.services.redshift.model.Tag> effectiveDesiredTags = Tagging.translateTagsToSdk(desiredTags);

        final Collection<software.amazon.awssdk.services.redshift.model.Tag> tagsToRemove = Tagging.exclude(effectivePreviousTags, effectiveDesiredTags);
        final Collection<software.amazon.awssdk.services.redshift.model.Tag> tagsToAdd = Tagging.exclude(effectiveDesiredTags, effectivePreviousTags);

        if (tagsToAdd.isEmpty() && tagsToRemove.isEmpty()) {
            return progress;
        }

        String arn = progress.getCallbackContext().getIntegrationArn();
        if (arn == null) {
            ProgressEvent<ResourceModel, CallbackContext> progressEvent = fetchIntegrationArn(proxy, RedshiftClientProxyClient, progress);
            if (progressEvent.isFailed()) {
                return progressEvent;
            }
            arn = progressEvent.getCallbackContext().getIntegrationArn();
        }

        try {
            Tagging.deleteTags(RedshiftClientProxyClient, arn, Tagging.translateTagsToSdk(tagsToRemove));
            Tagging.createTags(RedshiftClientProxyClient, arn, Tagging.translateTagsToSdk(tagsToAdd));
        } catch (Exception exception) {
            return handleIntegrationException(exception);
        }

        return progress;
    }

}
