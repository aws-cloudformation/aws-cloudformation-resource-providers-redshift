package software.amazon.redshift.clusterparametergroup;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.RandomStringUtils;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterParameterGroupAlreadyExistsException;
import software.amazon.awssdk.services.redshift.model.ClusterParameterGroupQuotaExceededException;
import software.amazon.awssdk.services.redshift.model.CreateClusterParameterGroupRequest;
import software.amazon.awssdk.services.redshift.model.CreateClusterParameterGroupResponse;
import software.amazon.awssdk.services.redshift.model.InvalidTagException;
import software.amazon.awssdk.services.redshift.model.TagLimitExceededException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.resource.IdentifierUtils;

import java.util.Optional;
import java.util.UUID;
import java.util.Map;
import java.util.Collections;
import com.google.common.collect.Maps;


public class CreateHandler extends BaseHandlerStd {
    private static final int MAX_CLUSTER_PARAMETER_GROUP_NAME_LENGTH = 255;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<RedshiftClient> proxyClient,
            final Logger logger) {

        this.logger = logger;

        final ResourceModel resourceModel = request.getDesiredResourceState();

        if (StringUtils.isBlank(resourceModel.getParameterGroupName())) {
            logger.log(String.format("%s Updating cluster paramater group identifier", ResourceModel.TYPE_NAME));
            resourceModel.setParameterGroupName(IdentifierUtils.generateResourceIdentifier(
                    ObjectUtils.defaultIfNull(request.getStackId(), RandomStringUtils.randomAlphabetic(1)),
                    ObjectUtils.defaultIfNull(request.getLogicalResourceIdentifier(), UUID.randomUUID().toString()),
                    ObjectUtils.defaultIfNull(request.getClientRequestToken(), UUID.randomUUID().toString()),
                    MAX_CLUSTER_PARAMETER_GROUP_NAME_LENGTH).toLowerCase());
        }

        //Resource level + stack level tags
        Map<String, String> convertedTags = Translator.translateFromResourceModelToSdkTags(resourceModel.getTags());
        Map<String, String> mergedTags = Maps.newHashMap();

        mergedTags.putAll(Optional.ofNullable(request.getDesiredResourceTags()).orElse(Collections.emptyMap()));
        mergedTags.putAll(Optional.ofNullable(convertedTags).orElse(Collections.emptyMap()));

        return ProgressEvent.progress(resourceModel, callbackContext)
                .then(progress -> proxy.initiate(String.format("%s::Create", CALL_GRAPH_TYPE_NAME), proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                        .translateToServiceRequest(model -> Translator.translateToCreateRequest(resourceModel, mergedTags))
                        .makeServiceCall(this::createClusterParameterGroup)
                        .handleError(this::createClusterParameterGroupErrorHandler)
                        .progress()
                )

                .then(progress -> new UpdateHandler().handleRequest(proxy,
                        request.toBuilder()
                                .desiredResourceState(progress.getResourceModel())
                                .build(),
                        callbackContext,
                        proxyClient,
                        logger));
    }

    private CreateClusterParameterGroupResponse createClusterParameterGroup(final CreateClusterParameterGroupRequest awsRequest,
                                                                            final ProxyClient<RedshiftClient> proxyClient) {
        CreateClusterParameterGroupResponse awsResponse;
        awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::createClusterParameterGroup);

        logger.log(String.format("%s successfully created.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }

    private ProgressEvent<ResourceModel, CallbackContext> createClusterParameterGroupErrorHandler(final CreateClusterParameterGroupRequest awsRequest,
                                                                                                  final Exception exception,
                                                                                                  final ProxyClient<RedshiftClient> client,
                                                                                                  final ResourceModel model,
                                                                                                  final CallbackContext context) {
        if (exception instanceof ClusterParameterGroupAlreadyExistsException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.AlreadyExists);

        } else if (exception instanceof ClusterParameterGroupQuotaExceededException ||
                exception instanceof TagLimitExceededException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.ServiceLimitExceeded);

        } else if (exception instanceof InvalidTagException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.InvalidRequest);

        } else {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.GeneralServiceException);
        }
    }
}
