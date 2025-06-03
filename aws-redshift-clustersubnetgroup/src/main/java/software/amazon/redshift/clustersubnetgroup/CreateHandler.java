package software.amazon.redshift.clustersubnetgroup;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetGroupAlreadyExistsException;
import software.amazon.awssdk.services.redshift.model.CreateClusterSubnetGroupRequest;
import software.amazon.awssdk.services.redshift.model.CreateClusterSubnetGroupResponse;
import software.amazon.awssdk.services.redshift.model.InvalidTagException;
import software.amazon.awssdk.services.redshift.model.TagLimitExceededException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.resource.IdentifierUtils;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Collections;
import com.google.common.collect.Maps;

public class CreateHandler extends BaseHandlerStd {
    private static final int MAX_SUBNET_GROUP_NAME_LENGTH = 255;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<RedshiftClient> proxyClient,
            final Logger logger) {

        this.logger = logger;

        final ResourceModel model = request.getDesiredResourceState();

        // Generate name if not provided
        if (StringUtils.isBlank(model.getClusterSubnetGroupName())) {
            logger.log(String.format("%s Updating cluster subnet group identifier", ResourceModel.TYPE_NAME));
            model.setClusterSubnetGroupName(IdentifierUtils.generateResourceIdentifier(
                    ObjectUtils.defaultIfNull(request.getStackId(), RandomStringUtils.randomAlphabetic(1)),
                    ObjectUtils.defaultIfNull(request.getLogicalResourceIdentifier(), UUID.randomUUID().toString()),
                    ObjectUtils.defaultIfNull(request.getClientRequestToken(), UUID.randomUUID().toString()),
                    MAX_SUBNET_GROUP_NAME_LENGTH).toLowerCase());
        }

        // Resource level + stack level tags
        Map<String, String> convertedTags = Translator.translateFromResourceModelToSdkTags(model.getTags());
        Map<String, String> mergedTags = Maps.newHashMap();

        mergedTags.putAll(Optional.ofNullable(request.getDesiredResourceTags()).orElse(Collections.emptyMap()));
        mergedTags.putAll(Optional.ofNullable(convertedTags).orElse(Collections.emptyMap()));

        return ProgressEvent.progress(model, callbackContext)
                .then(progress -> proxy.initiate(String.format("%s::Create", CALL_GRAPH_TYPE_NAME), proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                        .translateToServiceRequest(rm -> Translator.translateToCreateRequest(rm.getClusterSubnetGroupName(), rm, mergedTags))
                        .makeServiceCall(this::createClusterSubnetGroup)
                        .handleError(this::createClusterSubnetGroupErrorHandler)
                        .progress()
                )
                .then(progress -> {
                    model.setClusterSubnetGroupName(progress.getResourceModel().getClusterSubnetGroupName());
                    return new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger);
                });
    }

    private CreateClusterSubnetGroupResponse createClusterSubnetGroup(
            final CreateClusterSubnetGroupRequest awsRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        CreateClusterSubnetGroupResponse awsResponse;
        awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::createClusterSubnetGroup);

        logger.log(String.format("%s successfully created.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }

    private ProgressEvent<ResourceModel, CallbackContext> createClusterSubnetGroupErrorHandler(
            final CreateClusterSubnetGroupRequest awsRequest,
            final Exception exception,
            final ProxyClient<RedshiftClient> client,
            final ResourceModel model,
            final CallbackContext context) {

        if (exception instanceof ClusterSubnetGroupAlreadyExistsException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.AlreadyExists);
        } else if (exception instanceof TagLimitExceededException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.ServiceLimitExceeded);
        } else if (exception instanceof InvalidTagException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.InvalidRequest);
        } else {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.GeneralServiceException);
        }
    }
}