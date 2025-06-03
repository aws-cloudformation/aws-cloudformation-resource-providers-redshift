package software.amazon.redshift.clustersubnetgroup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.CreateClusterSubnetGroupRequest;
import software.amazon.awssdk.services.redshift.model.CreateClusterSubnetGroupResponse;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSubnetGroupsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSubnetGroupsResponse;
import software.amazon.awssdk.services.redshift.model.DescribeTagsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeTagsResponse;
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
import static software.amazon.redshift.clustersubnetgroup.TestUtils.*;
import static software.amazon.redshift.clustersubnetgroup.TestUtils.MODEL_WITH_TAGS;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends AbstractTestBase {

    @Mock
    RedshiftClient sdkClient;
    @Mock
    private AmazonWebServicesClientProxy proxy;
    @Mock
    private ProxyClient<RedshiftClient> proxyClient;
    private CreateHandler handler;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(RedshiftClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
        handler = new CreateHandler();
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(MODEL_WITH_TAGS)
                .region(AWS_REGION)
                .desiredResourceTags(DESIRED_RESOURCE_TAGS)
                .logicalResourceIdentifier("logicalId")
                .clientRequestToken("token")
                .build();

        when(proxyClient.client().createClusterSubnetGroup(any(CreateClusterSubnetGroupRequest.class)))
                .thenReturn(CreateClusterSubnetGroupResponse.builder()
                        .clusterSubnetGroup(BASIC_CLUSTER_SUBNET_GROUP)
                        .build());

        when(proxyClient.client().describeClusterSubnetGroups(any(DescribeClusterSubnetGroupsRequest.class)))
                .thenReturn(DescribeClusterSubnetGroupsResponse.builder()
                        .clusterSubnetGroups(BASIC_CLUSTER_SUBNET_GROUP)
                        .build());

        when(proxyClient.client().describeTags(any(DescribeTagsRequest.class)))
                .thenReturn(DESCRIBE_TAGS_RESPONSE);

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client()).createClusterSubnetGroup(any(CreateClusterSubnetGroupRequest.class));
        verify(proxyClient.client()).describeClusterSubnetGroups(any(DescribeClusterSubnetGroupsRequest.class));
    }

    @Test
    public void handleRequest_GeneratedName() {
        final ResourceModel model = ResourceModel.builder()
                .description(DESCRIPTION)
                .subnetIds(SUBNET_IDS)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .region(AWS_REGION)
                .logicalResourceIdentifier("logicalId")
                .clientRequestToken("token")
                .build();

        when(proxyClient.client().createClusterSubnetGroup(any(CreateClusterSubnetGroupRequest.class)))
                .thenReturn(CreateClusterSubnetGroupResponse.builder()
                        .clusterSubnetGroup(BASIC_CLUSTER_SUBNET_GROUP)
                        .build());

        when(proxyClient.client().describeClusterSubnetGroups(any(DescribeClusterSubnetGroupsRequest.class)))
                .thenReturn(DescribeClusterSubnetGroupsResponse.builder()
                        .clusterSubnetGroups(BASIC_CLUSTER_SUBNET_GROUP)
                        .build());

        when(proxyClient.client().describeTags(any(DescribeTagsRequest.class)))
                .thenReturn(DescribeTagsResponse.builder().build());

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNotNull();
        assertThat(response.getResourceModel().getClusterSubnetGroupName()).isNotNull();
    }
}