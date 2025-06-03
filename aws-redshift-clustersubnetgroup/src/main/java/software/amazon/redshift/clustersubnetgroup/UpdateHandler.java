package software.amazon.redshift.clustersubnetgroup;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetQuotaExceededException;
import software.amazon.awssdk.services.redshift.model.InvalidSubnetException;
import software.amazon.awssdk.services.redshift.model.InvalidTagException;
import software.amazon.awssdk.services.redshift.model.ModifyClusterSubnetGroupResponse;
import software.amazon.awssdk.services.redshift.model.ResourceNotFoundException;
import software.amazon.awssdk.services.redshift.model.UnauthorizedOperationException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Collections;

public class UpdateHandler extends BaseHandlerStd {

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<RedshiftClient> proxyClient,
            final Logger logger) {

        this.logger = logger;
        final ResourceModel desiredResourceState = request.getDesiredResourceState();
        final String resourceName = String.format("arn:%s:redshift:%s:%s:subnetgroup:%s",
                request.getAwsPartition(),
                request.getRegion(),
                request.getAwsAccountId(),
                desiredResourceState.getClusterSubnetGroupName());

        // Handle tags
        Map<String, String> allDesiredTags = new HashMap<>();
        allDesiredTags.putAll(Optional.ofNullable(request.getDesiredResourceTags()).orElse(Collections.emptyMap()));
        allDesiredTags.putAll(Optional.ofNullable(
                        Translator.translateFromResourceModelToSdkTags(desiredResourceState.getTags()))
                .orElse(Collections.emptyMap()));
        List<software.amazon.redshift.clustersubnetgroup.Tag> desiredTags =
                Translator.translateTagsMapToTagCollection(allDesiredTags);

        List<software.amazon.redshift.clustersubnetgroup.Tag> previousTags =
                request.getPreviousResourceState() == null ?
                        null : request.getPreviousResourceState().getTags();
        Map<String, String> allPreviousTags = new HashMap<>();
        allPreviousTags.putAll(Optional.ofNullable(request.getPreviousResourceTags()).orElse(Collections.emptyMap()));
        allPreviousTags.putAll(Optional.ofNullable(Translator.translateFromResourceModelToSdkTags(previousTags))
                .orElse(Collections.emptyMap()));
        List<software.amazon.redshift.clustersubnetgroup.Tag> currentTags =
                Translator.translateTagsMapToTagCollection(allPreviousTags);

        return ProgressEvent.progress(desiredResourceState, callbackContext)
                // Read current tags
                .then(progress -> proxy.initiate(String.format("%s::Update::ReadTags", CALL_GRAPH_TYPE_NAME), proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                        .translateToServiceRequest(rm -> Translator.translateToReadTagsRequest(resourceName))
                        .makeServiceCall(this::readTags)
                        .handleError(this::operateTagsErrorHandler)
                        .done((tagsRequest, tagsResponse, client, model, context) -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                                .callbackContext(callbackContext)
                                .callbackDelaySeconds(0)
                                .resourceModel(Translator.translateFromReadTagsResponse(model, tagsResponse))
                                .status(OperationStatus.IN_PROGRESS)
                                .build()))
                // Update tags
                .then(progress -> proxy.initiate(String.format("%s::Update::UpdateTags", CALL_GRAPH_TYPE_NAME), proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                        .translateToServiceRequest(model -> Translator.translateToUpdateTagsRequest(desiredTags, currentTags, resourceName))
                        .makeServiceCall(this::updateTags)
                        .handleError(this::operateTagsErrorHandler)
                        .done((tagsRequest, tagsResponse, client, model, context) -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                                .callbackContext(callbackContext)
                                .callbackDelaySeconds(0)
                                .resourceModel(desiredResourceState)
                                .status(OperationStatus.IN_PROGRESS)
                                .build()))
                // Update subnet group
                .then(progress -> proxy.initiate(String.format("%s::Update::SubnetGroup", CALL_GRAPH_TYPE_NAME), proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                        .translateToServiceRequest(Translator::translateToUpdateRequest)
                        .makeServiceCall(this::modifyClusterSubnetGroup)
                        .handleError(this::handleError)
                        .progress())
                // Read the final state
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private ModifyClusterSubnetGroupResponse modifyClusterSubnetGroup(
            final software.amazon.awssdk.services.redshift.model.ModifyClusterSubnetGroupRequest awsRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        ModifyClusterSubnetGroupResponse response = proxyClient.injectCredentialsAndInvokeV2(
                awsRequest, proxyClient.client()::modifyClusterSubnetGroup);
        logger.log(String.format("%s has successfully been updated.", ResourceModel.TYPE_NAME));
        return response;
    }

    private ProgressEvent<ResourceModel, CallbackContext> handleError(
            final software.amazon.awssdk.services.redshift.model.ModifyClusterSubnetGroupRequest awsRequest,
            final Exception exception,
            final ProxyClient<RedshiftClient> client,
            final ResourceModel model,
            final CallbackContext context) {

        if (exception instanceof ClusterSubnetGroupNotFoundException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.NotFound);
        } else if (exception instanceof ClusterSubnetQuotaExceededException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.ServiceLimitExceeded);
        } else if (exception instanceof InvalidSubnetException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.InvalidRequest);
        } else if (exception instanceof UnauthorizedOperationException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.AccessDenied);
        }
        return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.GeneralServiceException);
    }
}