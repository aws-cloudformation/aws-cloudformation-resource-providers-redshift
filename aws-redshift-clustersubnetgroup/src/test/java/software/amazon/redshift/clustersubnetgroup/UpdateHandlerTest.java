package software.amazon.redshift.clustersubnetgroup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.CreateTagsRequest;
import software.amazon.awssdk.services.redshift.model.CreateTagsResponse;
import software.amazon.awssdk.services.redshift.model.DeleteTagsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSubnetGroupsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSubnetGroupsResponse;
import software.amazon.awssdk.services.redshift.model.DescribeTagsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeTagsResponse;
import software.amazon.awssdk.services.redshift.model.ModifyClusterSubnetGroupRequest;
import software.amazon.awssdk.services.redshift.model.ModifyClusterSubnetGroupResponse;
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
import static software.amazon.redshift.clustersubnetgroup.TestUtils.AWS_REGION;
import static software.amazon.redshift.clustersubnetgroup.TestUtils.BASIC_CLUSTER_SUBNET_GROUP;
import static software.amazon.redshift.clustersubnetgroup.TestUtils.MODEL_WITH_TAGS;
import static software.amazon.redshift.clustersubnetgroup.TestUtils.DESIRED_RESOURCE_TAGS;
import static software.amazon.redshift.clustersubnetgroup.TestUtils.PREVIOUS_RESOURCE_TAGS;
import static software.amazon.redshift.clustersubnetgroup.TestUtils.DESCRIBE_TAGS_RESPONSE_CREATING;

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
                .desiredResourceState(MODEL_WITH_TAGS)
                .desiredResourceTags(DESIRED_RESOURCE_TAGS)
                .previousResourceTags(PREVIOUS_RESOURCE_TAGS)
                .region(AWS_REGION)
                .logicalResourceIdentifier("logicalId")
                .clientRequestToken("token")
                .build();

        when(proxyClient.client().modifyClusterSubnetGroup(any(ModifyClusterSubnetGroupRequest.class)))
                .thenReturn(ModifyClusterSubnetGroupResponse.builder()
                        .clusterSubnetGroup(BASIC_CLUSTER_SUBNET_GROUP)
                        .build());

        when(proxyClient.client().describeTags(any(DescribeTagsRequest.class)))
                .thenReturn(DESCRIBE_TAGS_RESPONSE_CREATING);

        when(proxyClient.client().describeClusterSubnetGroups(any(DescribeClusterSubnetGroupsRequest.class)))
                .thenReturn(DescribeClusterSubnetGroupsResponse.builder()
                        .clusterSubnetGroups(BASIC_CLUSTER_SUBNET_GROUP)
                        .build());

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

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
                .desiredResourceState(MODEL_WITH_TAGS)
                .desiredResourceTags(DESIRED_RESOURCE_TAGS)
                .region(AWS_REGION)
                .logicalResourceIdentifier("logicalId")
                .clientRequestToken("token")
                .build();

        when(proxyClient.client().describeTags(any(DescribeTagsRequest.class)))
                .thenReturn(DescribeTagsResponse.builder().build());

        when(proxyClient.client().createTags(any(CreateTagsRequest.class)))
                .thenReturn(CreateTagsResponse.builder().build());

        when(proxyClient.client().modifyClusterSubnetGroup(any(ModifyClusterSubnetGroupRequest.class)))
                .thenReturn(ModifyClusterSubnetGroupResponse.builder()
                        .clusterSubnetGroup(BASIC_CLUSTER_SUBNET_GROUP)
                        .build());

        when(proxyClient.client().describeClusterSubnetGroups(any(DescribeClusterSubnetGroupsRequest.class)))
                .thenReturn(DescribeClusterSubnetGroupsResponse.builder()
                        .clusterSubnetGroups(BASIC_CLUSTER_SUBNET_GROUP)
                        .build());

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_NotFound() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(MODEL_WITH_TAGS)
                .desiredResourceTags(DESIRED_RESOURCE_TAGS)
                .region(AWS_REGION)
                .build();

        when(proxyClient.client().modifyClusterSubnetGroup(any(ModifyClusterSubnetGroupRequest.class)))
                .thenThrow(ClusterSubnetGroupNotFoundException.class);

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isNotNull();
    }
}
