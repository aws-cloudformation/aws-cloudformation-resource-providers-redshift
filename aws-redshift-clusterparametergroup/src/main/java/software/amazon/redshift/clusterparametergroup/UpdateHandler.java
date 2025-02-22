package software.amazon.redshift.clusterparametergroup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class UpdateHandler extends BaseHandlerStd {
    public static final String NEED_TO_BE_RESET = "needToBeReset";
    private static final String WLM_JSON_CONFIGURATION = "wlm_json_configuration";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<RedshiftClient> proxyClient,
            final Logger logger) {

        this.logger = logger;
        final ResourceModel desiredResourceState = request.getDesiredResourceState();
        final String resourceName = String.format("arn:%s:redshift:%s:%s:parametergroup:%s", request.getAwsPartition(), request.getRegion(), request.getAwsAccountId(), desiredResourceState.getParameterGroupName());
        Map<String, String> allDesiredTags = new HashMap<>();
        allDesiredTags.putAll(Optional.ofNullable(request.getDesiredResourceTags()).orElse(Collections.emptyMap()));
        allDesiredTags.putAll(Optional.ofNullable(
                        Translator.translateFromResourceModelToSdkTags(desiredResourceState.getTags()))
                .orElse(Collections.emptyMap()));
        List<Tag> desiredTags = Translator.translateTagsMapToTagCollection(allDesiredTags);


        List<Tag> previousTags = request.getPreviousResourceState() == null ? null : request.getPreviousResourceState().getTags();
        Map<String, String> allPreviousTags = new HashMap<>();
        allPreviousTags.putAll(Optional.ofNullable(request.getPreviousResourceTags()).orElse(Collections.emptyMap()));
        allPreviousTags.putAll(Optional.ofNullable(Translator.translateFromResourceModelToSdkTags(previousTags))
                .orElse(Collections.emptyMap()));
        List<Tag> currentTags = Translator.translateTagsMapToTagCollection(allPreviousTags);

        return ProgressEvent.progress(desiredResourceState, callbackContext)
                .then(progress -> proxy.initiate(String.format("%s::Update::ReadTags", CALL_GRAPH_TYPE_NAME), proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                        .translateToServiceRequest(resourceModel -> Translator.translateToReadTagsRequest(resourceName))
                        .makeServiceCall(this::readTags)
                        .handleError(this::operateTagsErrorHandler)
                        .done((tagsRequest, tagsResponse, client, model, context) -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                                .callbackContext(callbackContext)
                                .callbackDelaySeconds(0)
                                .resourceModel(Translator.translateFromReadTagsResponse(model, tagsResponse))
                                .status(OperationStatus.IN_PROGRESS)
                                .build()))
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

    /*
    compares the desired parameters and the previous parameters,
    calculates which parameters need to be reset (value set to NEED_TO_BE_RESET),
    and which parameter values need to be updated
     */
    private ResourceModel getUpdatableResourceModel(ResourceModel desiredModel, ResourceModel previousModel) {
        logger.log("DesiredModel parameters: " + desiredModel.getParameters() + "\nPreviousModel parameters: " + previousModel.getParameters());

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

        /*
        like before, we assume there's no duplicated parameters like [{key1: value1}, {key1: value2}]
         */
        Function<List<Parameter>, Map<String, String>> paramsToMap = list -> list.stream()
                .collect(Collectors.toMap(Parameter::getParameterName, Parameter::getParameterValue));
        Map<String, String> desiredParamKeyValueMap = paramsToMap.apply(desiredParameters);
        Map<String, String> previousParamKeyValueMap = paramsToMap.apply(previousParameters);

        /*
        previousParameters
        [
            { "auto_analyze": true },
            { "date_style": "ISO, MDY" },
            { "wlm_json_configuration": "[{key1: value1}]" }
        ]

        desiredParameters
        [
            { "statement_timeout": 1000 },
            { "date_style": "ISO, MDY" }, // value stays the same, will be ignored,
            { "wlm_json_configuration": "[{key1:        value1}]" } // needs to `JSON` compare with previous wlm_json_configuration (see code)
        ]

        updatableParameters
        [
            { "auto_analyze": NEED_TO_BE_RESET }, // exists in previous not in desired, value will be set to NEED_TO_BE_RESET
        ]
         */
        List<Parameter> updatableParameters = CollectionUtils.disjunction(desiredParameters, previousParameters)
                .stream()
                .filter(parameter -> {
                    /*
                    We only specially handle when wlm_json_configuration exists in both previous and desired parameters.

                    When both previous and desired parameters contain wlm_json_configuration,
                    we'll need to first sanitize both wlm_json_configurations (stringified JSON) then
                    check if they're equal.

                    For example, [{key:value}] is the same as [   {key: value}]

                    When only previous parameters contains wlm_json_configuration,
                    it means it needs to be reset.

                    When only desired parameters contains wlm_json_configuration,
                    we only need to apply the new one.
                     */
                    if (parameter.getParameterName().equals(WLM_JSON_CONFIGURATION)
                            && desiredParamKeyValueMap.containsKey(WLM_JSON_CONFIGURATION)
                            && previousParamKeyValueMap.containsKey(WLM_JSON_CONFIGURATION)
                    ) {
                        try {
                            final String desiredWlm = getSanitizedString(desiredParamKeyValueMap.get(WLM_JSON_CONFIGURATION));
                            final String previousWlm = getSanitizedString(previousParamKeyValueMap.get(WLM_JSON_CONFIGURATION));
                            /*
                            only when their sanitized values don't match, we update
                             */
                            return !desiredWlm.equals(previousWlm);
                        } catch (JsonProcessingException e) {
                            /*
                            ignore this exception for now to be consistent with existing behavior,
                            this invalid JSON will fail modifyParameterGroup API's JSON validation.

                            When I get a chance to verify the behaviors we should for sure
                            throw invalid JSON exception as soon as we can:

                            throw new CfnInvalidRequestException(e);
                             */
                            return true; // true meaning we'll modify wlm_json_configuration using the new one
                        }
                    }
                    // don't filter out any parameters by default
                    return true;
                })
                .map(parameter -> Parameter.builder()
                        .parameterName(parameter.getParameterName())
                        .parameterValue(desiredParamKeyValueMap.getOrDefault(parameter.getParameterName(), NEED_TO_BE_RESET))
                        .build())
                /*
                needs distinct because doing the map operation above can introduce the same Parameter,
                this happens when the same parameter value changes,
                in which this parameter key exists in both previous and desired parameters, but w/ diff values,
                after map() we'll have 2 parameters, whose values are all set to desired parameter's value
                 */
                .distinct()
                .collect(Collectors.toList());

        return desiredModel.toBuilder()
                .parameters(updatableParameters)
                .build();
    }

    private String getSanitizedString(final String str) throws JsonProcessingException {
        return OBJECT_MAPPER.readValue(str, JsonNode.class).toString();
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
