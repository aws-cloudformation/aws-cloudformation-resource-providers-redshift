package software.amazon.redshift.clusterparametergroup;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.CreateTagsRequest;
import software.amazon.awssdk.services.redshift.model.CreateTagsResponse;
import software.amazon.awssdk.services.redshift.model.DeleteTagsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterParameterGroupsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterParameterGroupsResponse;
import software.amazon.awssdk.services.redshift.model.DescribeClusterParametersRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterParametersResponse;
import software.amazon.awssdk.services.redshift.model.DescribeTagsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeTagsResponse;
import software.amazon.awssdk.services.redshift.model.ModifyClusterParameterGroupRequest;
import software.amazon.awssdk.services.redshift.model.ModifyClusterParameterGroupResponse;
import software.amazon.awssdk.services.redshift.model.ResetClusterParameterGroupRequest;
import software.amazon.awssdk.services.redshift.model.ResetClusterParameterGroupResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static software.amazon.redshift.clusterparametergroup.TestUtils.AWS_REGION;
import static software.amazon.redshift.clusterparametergroup.TestUtils.CLUSTER_PARAMETER_GROUP;
import static software.amazon.redshift.clusterparametergroup.TestUtils.COMPLETE_MODEL;
import static software.amazon.redshift.clusterparametergroup.TestUtils.DESCRIBE_TAGS_RESPONSE_CREATING;
import static software.amazon.redshift.clusterparametergroup.TestUtils.DESCRIPTION;
import static software.amazon.redshift.clusterparametergroup.TestUtils.DESIRED_PARAMETERS;
import static software.amazon.redshift.clusterparametergroup.TestUtils.DESIRED_RESOURCE_TAGS;
import static software.amazon.redshift.clusterparametergroup.TestUtils.PARAMETER_GROUP_FAMILY;
import static software.amazon.redshift.clusterparametergroup.TestUtils.PARAMETER_GROUP_NAME;
import static software.amazon.redshift.clusterparametergroup.TestUtils.PREVIOUS_PARAMETERS;
import static software.amazon.redshift.clusterparametergroup.TestUtils.TAGS;
import static software.amazon.redshift.clusterparametergroup.TestUtils.WLM_JSON_CONFIGURATION;
import static software.amazon.redshift.clusterparametergroup.TestUtils.getSdkParametersFromParameters;
import static software.amazon.redshift.clusterparametergroup.TestUtils.parametersEquals;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UpdateHandlerTest extends AbstractTestBase {

    @Mock
    RedshiftClient sdkClient;
    @Mock
    private AmazonWebServicesClientProxy proxy;
    @Mock
    private ProxyClient<RedshiftClient> proxyClient;
    private UpdateHandler handler;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(RedshiftClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
        handler = new UpdateHandler();
    }

    @Test
    public void handleRequest_UpdateTags() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(COMPLETE_MODEL)
                .desiredResourceTags(DESIRED_RESOURCE_TAGS)
                .region(AWS_REGION)
                .build();

        when(proxyClient.client().modifyClusterParameterGroup(any(ModifyClusterParameterGroupRequest.class)))
                .thenReturn(ModifyClusterParameterGroupResponse.builder()
                        .parameterGroupName(PARAMETER_GROUP_NAME)
                        .parameterGroupStatus("Your parameter group has been updated")
                        .build());

        when(proxyClient.client().describeTags(any(DescribeTagsRequest.class)))
                .thenReturn(DESCRIBE_TAGS_RESPONSE_CREATING);

        when(proxyClient.client().describeClusterParameters(any(DescribeClusterParametersRequest.class)))
                .thenReturn(DescribeClusterParametersResponse.builder().build());

        when(proxyClient.client().describeClusterParameterGroups(any(DescribeClusterParameterGroupsRequest.class)))
                .thenReturn(DescribeClusterParameterGroupsResponse.builder()
                        .parameterGroups(CLUSTER_PARAMETER_GROUP)
                        .build());

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        ArgumentCaptor<CreateTagsRequest> createTagArgument = ArgumentCaptor.forClass(CreateTagsRequest.class);
        verify(proxyClient.client()).createTags(createTagArgument.capture());
        ArgumentCaptor<DeleteTagsRequest> deleteTagArgument = ArgumentCaptor.forClass(DeleteTagsRequest.class);
        verify(proxyClient.client()).deleteTags(deleteTagArgument.capture());
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(COMPLETE_MODEL)
                .desiredResourceTags(DESIRED_RESOURCE_TAGS)
                .region(AWS_REGION)
                .build();

        when(proxyClient.client().describeTags(any(DescribeTagsRequest.class)))
                .thenReturn(DescribeTagsResponse.builder().build());

        when(proxyClient.client().createTags(any(CreateTagsRequest.class)))
                .thenReturn(CreateTagsResponse.builder().build());

        when(proxyClient.client().modifyClusterParameterGroup(any(ModifyClusterParameterGroupRequest.class)))
                .thenReturn(ModifyClusterParameterGroupResponse.builder()
                        .parameterGroupName(PARAMETER_GROUP_NAME)
                        .parameterGroupStatus("Your parameter group has been updated")
                        .build());

        when(proxyClient.client().describeClusterParameters(any(DescribeClusterParametersRequest.class)))
                .thenReturn(DescribeClusterParametersResponse.builder()
                        .parameters(getSdkParametersFromParameters(PREVIOUS_PARAMETERS))
                        .build());

        when(proxyClient.client().describeClusterParameterGroups(any(DescribeClusterParameterGroupsRequest.class)))
                .thenReturn(DescribeClusterParameterGroupsResponse.builder()
                        .parameterGroups(CLUSTER_PARAMETER_GROUP)
                        .build());

        ArgumentCaptor<ModifyClusterParameterGroupRequest> cap = ArgumentCaptor.forClass(ModifyClusterParameterGroupRequest.class);
        when(proxyClient.client().modifyClusterParameterGroup(cap.capture()))
                .thenReturn(ModifyClusterParameterGroupResponse.builder().build());

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_jsonValidateException() {
        final ResourceModel invalidJSONModel = ResourceModel.builder()
                .parameterGroupName(PARAMETER_GROUP_NAME)
                .description(DESCRIPTION)
                .parameterGroupFamily(PARAMETER_GROUP_FAMILY)
                .tags(TAGS)
                .parameters(Arrays.asList(
                        Parameter.builder()
                                .parameterName("wlm_json_configuration")
                                .parameterValue("{invalid]")
                                .build()
                ))
                .build();

        final ResourceHandlerRequest<ResourceModel> invalidJSONRequest = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(invalidJSONModel)
                .desiredResourceTags(DESIRED_RESOURCE_TAGS)
                .region(AWS_REGION)
                .build();

        when(proxyClient.client().describeTags(any(DescribeTagsRequest.class)))
                .thenReturn(DescribeTagsResponse.builder().build());

        when(proxyClient.client().createTags(any(CreateTagsRequest.class)))
                .thenReturn(CreateTagsResponse.builder().build());

        when(proxyClient.client().modifyClusterParameterGroup(any(ModifyClusterParameterGroupRequest.class)))
                .thenReturn(ModifyClusterParameterGroupResponse.builder()
                        .parameterGroupName(PARAMETER_GROUP_NAME)
                        .parameterGroupStatus("Your parameter group has been updated")
                        .build());

        when(proxyClient.client().describeClusterParameters(any(DescribeClusterParametersRequest.class)))
                .thenReturn(DescribeClusterParametersResponse.builder()
                        .parameters(getSdkParametersFromParameters(PREVIOUS_PARAMETERS))
                        .build());

        when(proxyClient.client().describeClusterParameterGroups(any(DescribeClusterParameterGroupsRequest.class)))
                .thenReturn(DescribeClusterParameterGroupsResponse.builder()
                        .parameterGroups(CLUSTER_PARAMETER_GROUP)
                        .build());

        ArgumentCaptor<ModifyClusterParameterGroupRequest> cap = ArgumentCaptor.forClass(ModifyClusterParameterGroupRequest.class);
        when(proxyClient.client().modifyClusterParameterGroup(cap.capture()))
                .thenReturn(ModifyClusterParameterGroupResponse.builder().build());

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, invalidJSONRequest, new CallbackContext(), proxyClient, logger);
    }

    /*
    in each test case,
    Object[0] = previous parameters (parameters in previousModel)
    Object[1] = parameters with which to call modifyClusterParameterGroup
    Object[2] = parameters with which to call resetClusterParameterGroup

    desiredParameters always are (feel free to change to cover more):
        final static List<Parameter> DESIRED_PARAMETERS = Arrays.asList(
            Parameter.builder()
                    .parameterName("auto_analyze")
                    .parameterValue("true")
                    .build(),
            Parameter.builder()
                    .parameterName("datestyle")
                    .parameterValue("ISO, MDY")
                    .build(),
            Parameter.builder()
                    .parameterName("wlm_json_configuration")
                    .parameterValue("[{\"user_group\":\"example_user_group1\",\"query_group\": \"example_query_group1\", \"query_concurrency\":7},{\"query_concurrency\":5}]")
                    .build()
     */
    private static Stream<Object[]> parameterTestProvider() {
        return Stream.of(
                new Object[]{
                        Arrays.asList(
                                Parameter.builder()
                                        .parameterName("datestyle")
                                        .parameterValue("ISO, MDY")
                                        .build(),
                                Parameter.builder()
                                        .parameterName("wlm_json_configuration")
                                        // adding format changes to the json string,
                                        // we'll need to compare the actual JSON objects are equal functionally
                                        // as long as the sanitized version stays the same, we don't call modify
                                        .parameterValue("[  {\"user_group\":\"example_user_group1\",\"query_group\":\"example_query_group1\",  \"query_concurrency\":7},{\"query_concurrency\":5}]")
                                        .build()
                        ),
                        Arrays.asList(
                                Parameter.builder()
                                        .parameterName("auto_analyze")
                                        .parameterValue("true")
                                        .build()
                        ),
                        null
                },
                new Object[]{
                        Arrays.asList(
                                Parameter.builder()
                                        .parameterName("datestyle")
                                        .parameterValue("ISO, MDY")
                                        .build(),
                                Parameter.builder()
                                        .parameterName("wlm_json_configuration")
                                        // adding format changes to the json string,
                                        // we'll need to compare the actual JSON objects are equal functionally
                                        .parameterValue("[ {\"user_group\":\"example_user_group3\",\"query_group\":\"example_query_group1\",  \"query_concurrency\":7},{\"query_concurrency\":5}]")
                                        .build()
                        ),
                        Arrays.asList(
                                Parameter.builder()
                                        .parameterName("auto_analyze")
                                        .parameterValue("true")
                                        .build(),
                                Parameter.builder()
                                        .parameterName("wlm_json_configuration")
                                        // adding format changes to the json string,
                                        // we'll need to compare the actual JSON objects are equal functionally
                                        .parameterValue(WLM_JSON_CONFIGURATION)
                                        .build()
                        ),
                        null
                },
                new Object[]{ // reset
                        Arrays.asList(
                                Parameter.builder()
                                        .parameterName("to_be_reset")
                                        .parameterValue("some_randome_value")
                                        .build()
                        ),
                        DESIRED_PARAMETERS,
                        Arrays.asList(
                                Parameter.builder()
                                        .parameterName("to_be_reset")
                                        .parameterValue("needToBeReset")
                                        .build()
                        )
                }
        );
    }

    @ParameterizedTest
    @MethodSource("parameterTestProvider")
    public void testParameterGroupUpdate(List<Parameter> previousParameters, List<Parameter> modifyParameters, List<Parameter> resetParameters) {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(COMPLETE_MODEL)
                .desiredResourceTags(DESIRED_RESOURCE_TAGS)
                .region(AWS_REGION)
                .build();

        when(proxyClient.client().describeTags(any(DescribeTagsRequest.class)))
                .thenReturn(DescribeTagsResponse.builder().build());

        when(proxyClient.client().createTags(any(CreateTagsRequest.class)))
                .thenReturn(CreateTagsResponse.builder().build());

        when(proxyClient.client().describeClusterParameters(any(DescribeClusterParametersRequest.class)))
                .thenReturn(DescribeClusterParametersResponse.builder()
                        .parameters(getSdkParametersFromParameters(previousParameters))
                        .build());

        when(proxyClient.client().describeClusterParameterGroups(any(DescribeClusterParameterGroupsRequest.class)))
                .thenReturn(DescribeClusterParameterGroupsResponse.builder()
                        .parameterGroups(CLUSTER_PARAMETER_GROUP)
                        .build());

        ArgumentCaptor<ModifyClusterParameterGroupRequest> modifyCaptor = ArgumentCaptor.forClass(ModifyClusterParameterGroupRequest.class);
        when(proxyClient.client().modifyClusterParameterGroup(modifyCaptor.capture()))
                .thenReturn(ModifyClusterParameterGroupResponse.builder().build());

        ArgumentCaptor<ResetClusterParameterGroupRequest> resetCaptor = ArgumentCaptor.forClass(ResetClusterParameterGroupRequest.class);
        when(proxyClient.client().resetClusterParameterGroup(resetCaptor.capture()))
                .thenReturn(ResetClusterParameterGroupResponse.builder().build());

        handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        if (modifyParameters != null) {
            Assertions.assertTrue(parametersEquals(modifyCaptor.getValue().parameters(), getSdkParametersFromParameters(modifyParameters)));
        }
        if (resetParameters != null) {
            Assertions.assertTrue(parametersEquals(resetCaptor.getValue().parameters(), getSdkParametersFromParameters(resetParameters)));
        }
    }
}
