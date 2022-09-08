package software.amazon.redshift.clusterparametergroup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static software.amazon.redshift.clusterparametergroup.TestUtils.AWS_REGION;
import static software.amazon.redshift.clusterparametergroup.TestUtils.CLUSTER_PARAMETER_GROUP;
import static software.amazon.redshift.clusterparametergroup.TestUtils.COMPLETE_MODEL;
import static software.amazon.redshift.clusterparametergroup.TestUtils.DESCRIBE_TAGS_RESPONSE_CREATING;
import static software.amazon.redshift.clusterparametergroup.TestUtils.DESIRED_RESOURCE_TAGS;
import static software.amazon.redshift.clusterparametergroup.TestUtils.PARAMETER_GROUP_NAME;

@ExtendWith(MockitoExtension.class)
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
}
