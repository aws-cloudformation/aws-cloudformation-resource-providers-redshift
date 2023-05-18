package software.amazon.redshift.clusterparametergroup;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterParameterGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.CreateTagsResponse;
import software.amazon.awssdk.services.redshift.model.DescribeClusterParametersRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterParametersResponse;
import software.amazon.awssdk.services.redshift.model.DescribeTagsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeTagsResponse;
import software.amazon.awssdk.services.redshift.model.InvalidClusterParameterGroupStateException;
import software.amazon.awssdk.services.redshift.model.InvalidClusterStateException;
import software.amazon.awssdk.services.redshift.model.InvalidTagException;
import software.amazon.awssdk.services.redshift.model.ModifyClusterParameterGroupRequest;
import software.amazon.awssdk.services.redshift.model.ModifyClusterParameterGroupResponse;
import software.amazon.awssdk.services.redshift.model.ResetClusterParameterGroupRequest;
import software.amazon.awssdk.services.redshift.model.ResetClusterParameterGroupResponse;
import software.amazon.awssdk.services.redshift.model.ResourceNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UpdateHandler extends BaseHandlerStd {
    public static final String NEED_TO_BE_RESET = "needToBeReset";
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<RedshiftClient> proxyClient,
            final Logger logger) {

        this.logger = logger;
        final String resourceName = String.format("arn:%s:redshift:%s:%s:parametergroup:%s", request.getAwsPartition(), request.getRegion(), request.getAwsAccountId(), request.getDesiredResourceState().getParameterGroupName());

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress -> proxy.initiate(String.format("%s::Update::ReadTags", CALL_GRAPH_TYPE_NAME), proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                        .translateToServiceRequest(resourceModel -> Translator.translateToReadTagsRequest(resourceName))
                        .makeServiceCall(this::readTags)
                        .handleError(this::operateTagsErrorHandler)
                        .done((tagsRequest, tagsResponse, client, model, context) -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                                .callbackContext(callbackContext)
                                .callbackDelaySeconds(0)
                                .resourceModel(Translator.translateFromReadTagsResponse(tagsResponse))
                                .status(OperationStatus.IN_PROGRESS)
                                .build()))

                .then(progress -> proxy.initiate(String.format("%s::Update::UpdateTags", CALL_GRAPH_TYPE_NAME), proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                        .translateToServiceRequest(resourceModel -> Translator.translateToUpdateTagsRequest(request.getDesiredResourceState(), resourceModel, resourceName))
                        .makeServiceCall(this::updateTags)
                        .handleError(this::operateTagsErrorHandler)
                        .done((tagsRequest, tagsResponse, client, model, context) -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                                .callbackContext(callbackContext)
                                .callbackDelaySeconds(0)
                                .resourceModel(request.getDesiredResourceState())
                                .status(OperationStatus.IN_PROGRESS)
                                .build()))

                .then(progress -> proxy.initiate(String.format("%s::Update::ReadParameters", CALL_GRAPH_TYPE_NAME), proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                        .translateToServiceRequest(Translator::translateToReadParametersRequest)
                        .makeServiceCall(this::describeClusterParameters)
                        .handleError(this::describeClusterParametersErrorHandler)
                        .done((readRequest, readResponse, client, model, context) -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                                .callbackContext(context)
                                .callbackDelaySeconds(0)
                                .resourceModel(getUpdatableResourceModel(model, Translator.translateFromReadParametersResponse(readResponse, model)))
                                .status(OperationStatus.IN_PROGRESS)
                                .build()))

                .then(progress -> proxy.initiate(String.format("%s::Update::ResetParameters", CALL_GRAPH_TYPE_NAME), proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                        .translateToServiceRequest(Translator::translateToResetRequest)
                        .makeServiceCall(this::resetClusterParameterGroup)
                        .handleError(this::resetClusterParameterGroupErrorHandler)
                        .progress())

                .then(progress -> proxy.initiate(String.format("%s::Update::UpdateParameters", CALL_GRAPH_TYPE_NAME), proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                        .translateToServiceRequest(Translator::translateToUpdateRequest)
                        .makeServiceCall(this::modifyClusterParameterGroup)
                        .handleError(this::modifyClusterParameterGroupErrorHandler)
                        .progress())

                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private ResourceModel getUpdatableResourceModel(ResourceModel desiredModel, ResourceModel previousModel) {
        Function<List<Parameter>, List<Parameter>> lowerCaseParameterName = (raw) -> Optional.ofNullable(raw)
                .map(parameters -> parameters
                        .stream()
                        .map(parameter -> Parameter.builder()
                                .parameterName(StringUtils.lowerCase(parameter.getParameterName()))
                                .parameterValue(parameter.getParameterValue())
                                .build())
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());

        List<Parameter> desiredParameters = lowerCaseParameterName.apply(desiredModel.getParameters());
        List<Parameter> previousParameters = lowerCaseParameterName.apply(previousModel.getParameters());

        for (Parameter desiredParam : desiredParameters) {
            logger.log(String.format("desiredParam: ParameterName: %s ParameterValue: %s",
                    desiredParam.getParameterName(),
                    desiredParam.getParameterValue())
            );
        }

        for (Parameter previousParam : desiredParameters) {
            logger.log(String.format("previousParam: ParameterName: %s ParameterValue: %s",
                    previousParam.getParameterName(),
                    previousParam.getParameterValue())
            );
        }

        /*
        Any added parameters to previousParameters will be updated to the parameter group;
        any removed parameters from previousParameters will be reset to the default value.
         */
        List<Parameter> updatableParameters = CollectionUtils.disjunction(desiredParameters, previousParameters)
                .stream()
                .map(parameter -> desiredParameters
                        .stream()
                        .filter(d -> StringUtils.equalsIgnoreCase(d.getParameterName(), parameter.getParameterName()))
                        .findAny()
                        .orElse(Parameter.builder()
                                .parameterName(parameter.getParameterName())
                                .parameterValue(NEED_TO_BE_RESET)
                                .build()))
                .distinct()
                .collect(Collectors.toList());

        logger.log("The following parameters will be updated or reset:");
        for (Parameter updatableParam : updatableParameters) {
            logger.log(String.format("updatableParam: ParameterName: %s ParameterValue: %s",
                    updatableParam.getParameterName(),
                    updatableParam.getParameterValue())
            );
        }

        return desiredModel.toBuilder()
            .parameters(updatableParameters)
            .build();
    }

    private DescribeClusterParametersResponse describeClusterParameters(final DescribeClusterParametersRequest awsRequest,
                                                                        final ProxyClient<RedshiftClient> proxyClient) {
        DescribeClusterParametersResponse awsResponse;
        awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeClusterParameters);

        logger.log(String.format("%s's Parameters has successfully been read.", ResourceModel.TYPE_NAME));

        if (awsResponse.hasParameters()) {
            for (software.amazon.awssdk.services.redshift.model.Parameter param : awsResponse.parameters()) {
                logger.log("Parameter from describeClusterParameter: " + param.toString());
            }
        }

        return awsResponse;
    }

    private ProgressEvent<ResourceModel, CallbackContext> describeClusterParametersErrorHandler(final DescribeClusterParametersRequest awsRequest,
                                                                                                final Exception exception,
                                                                                                final ProxyClient<RedshiftClient> client,
                                                                                                final ResourceModel model,
                                                                                                final CallbackContext context) {
        if (exception instanceof ClusterParameterGroupNotFoundException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.NotFound);

        } else {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.GeneralServiceException);
        }
    }

    private DescribeTagsResponse readTags(final DescribeTagsRequest awsRequest,
                                          final ProxyClient<RedshiftClient> proxyClient) {
        DescribeTagsResponse awsResponse;
        awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::describeTags);

        logger.log(String.format("%s's tags have successfully been read.", ResourceModel.TYPE_NAME));
        return awsResponse;
    }

    private CreateTagsResponse updateTags(final ModifyTagsRequest awsRequest,
                                          final ProxyClient<RedshiftClient> proxyClient) {
        CreateTagsResponse awsResponse = null;

        if (awsRequest.getDeleteOldTagsRequest().tagKeys().isEmpty()) {
            logger.log(String.format("No tags would be deleted for the resource: %s.", ResourceModel.TYPE_NAME));

        } else {
            proxyClient.injectCredentialsAndInvokeV2(awsRequest.getDeleteOldTagsRequest(), proxyClient.client()::deleteTags);
            logger.log(String.format("Delete tags for the resource: %s.", ResourceModel.TYPE_NAME));
        }

        if (awsRequest.getCreateNewTagsRequest().tags().isEmpty()) {
            logger.log(String.format("No tags would be created for the resource: %s.", ResourceModel.TYPE_NAME));

        } else {
            awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest.getCreateNewTagsRequest(), proxyClient.client()::createTags);
            logger.log(String.format("Create tags for the resource: %s.", ResourceModel.TYPE_NAME));
        }

        return awsResponse;
    }

    private ProgressEvent<ResourceModel, CallbackContext> operateTagsErrorHandler(final Object awsRequest,
                                                                                  final Exception exception,
                                                                                  final ProxyClient<RedshiftClient> client,
                                                                                  final ResourceModel model,
                                                                                  final CallbackContext context) {
        if (exception instanceof ResourceNotFoundException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.NotFound);

        } else if (exception instanceof InvalidTagException ||
                exception instanceof InvalidClusterStateException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.InvalidRequest);

        } else {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.GeneralServiceException);
        }
    }

    private ResetClusterParameterGroupResponse resetClusterParameterGroup(final ResetClusterParameterGroupRequest awsRequest,
                                                                          final ProxyClient<RedshiftClient> proxyClient) {
        return Optional.of(awsRequest)
                .filter(r -> !CollectionUtils.isEmpty(r.parameters()))
                .map(r -> {
                    ResetClusterParameterGroupResponse awsResponse;
                    awsResponse = proxyClient.injectCredentialsAndInvokeV2(r, proxyClient.client()::resetClusterParameterGroup);

                    logger.log(String.format("%s's Parameters has successfully been reset.", ResourceModel.TYPE_NAME));
                    return awsResponse;
                })
                .orElseGet(() -> {
                    logger.log(String.format("%s's Parameters has nothing to be reset.", ResourceModel.TYPE_NAME));
                    return ResetClusterParameterGroupResponse.builder().build();
                });
    }

    private ProgressEvent<ResourceModel, CallbackContext> resetClusterParameterGroupErrorHandler(final ResetClusterParameterGroupRequest awsRequest,
                                                                                                 final Exception exception,
                                                                                                 final ProxyClient<RedshiftClient> client,
                                                                                                 final ResourceModel model,
                                                                                                 final CallbackContext context) {
        if (exception instanceof ClusterParameterGroupNotFoundException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.NotFound);

        } else if (exception instanceof InvalidClusterParameterGroupStateException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.ResourceConflict);

        } else {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.GeneralServiceException);
        }
    }

    private ModifyClusterParameterGroupResponse modifyClusterParameterGroup(final ModifyClusterParameterGroupRequest awsRequest,
                                                                            final ProxyClient<RedshiftClient> proxyClient) {
        return Optional.of(awsRequest)
                .filter(r -> !CollectionUtils.isEmpty(r.parameters()))
                .map(r -> {
                    ModifyClusterParameterGroupResponse awsResponse;
                    awsResponse = proxyClient.injectCredentialsAndInvokeV2(r, proxyClient.client()::modifyClusterParameterGroup);

                    logger.log(String.format("%s's Parameters has successfully been updated.", ResourceModel.TYPE_NAME));
                    return awsResponse;
                })
                .orElseGet(() -> {
                    logger.log(String.format("%s's Parameters has nothing to be updated.", ResourceModel.TYPE_NAME));
                    return ModifyClusterParameterGroupResponse.builder().build();
                });
    }

    private ProgressEvent<ResourceModel, CallbackContext> modifyClusterParameterGroupErrorHandler(final ModifyClusterParameterGroupRequest awsRequest,
                                                                                                  final Exception exception,
                                                                                                  final ProxyClient<RedshiftClient> client,
                                                                                                  final ResourceModel model,
                                                                                                  final CallbackContext context) {
        if (exception instanceof ClusterParameterGroupNotFoundException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.NotFound);

        } else if (exception instanceof InvalidClusterParameterGroupStateException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.ResourceConflict);

        } else {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.GeneralServiceException);
        }
    }
}
