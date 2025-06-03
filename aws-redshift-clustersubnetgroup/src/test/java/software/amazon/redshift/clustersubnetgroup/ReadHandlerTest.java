package software.amazon.redshift.clustersubnetgroup;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSubnetGroupsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSubnetGroupsResponse;
import software.amazon.awssdk.services.redshift.model.DescribeTagsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeTagsResponse;
import software.amazon.awssdk.services.redshift.model.InvalidTagException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static software.amazon.redshift.clustersubnetgroup.TestUtils.AWS_ACCOUNT_ID;
import static software.amazon.redshift.clustersubnetgroup.TestUtils.AWS_REGION;
import static software.amazon.redshift.clustersubnetgroup.TestUtils.BASIC_CLUSTER_SUBNET_GROUP;
import static software.amazon.redshift.clustersubnetgroup.TestUtils.BASIC_MODEL;
import static software.amazon.redshift.clustersubnetgroup.TestUtils.DESCRIBE_TAGS_RESPONSE;
import static software.amazon.redshift.clustersubnetgroup.TestUtils.MODEL_WITH_TAGS;
import static software.amazon.redshift.clustersubnetgroup.TestUtils.TAGS;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<RedshiftClient> proxyClient;

    @Mock
    RedshiftClient sdkClient;

    private ReadHandler handler;

    @BeforeEach
    public void setup() {
        handler = new ReadHandler();
        sdkClient = mock(RedshiftClient.class);
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxyClient = MOCK_PROXY(proxy, sdkClient);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ResourceModel model = BASIC_MODEL;

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .region(AWS_REGION)
                .awsAccountId(AWS_ACCOUNT_ID)
                .build();

        when(proxyClient.client().describeClusterSubnetGroups(any(DescribeClusterSubnetGroupsRequest.class)))
                .thenReturn(DescribeClusterSubnetGroupsResponse.builder()
                        .clusterSubnetGroups(Arrays.asList(BASIC_CLUSTER_SUBNET_GROUP))
                        .build());

        when(proxyClient.client().describeTags(any(DescribeTagsRequest.class)))
                .thenReturn(DescribeTagsResponse.builder()
                        .taggedResources(Collections.emptyList())
                        .build());

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getResourceModel()).isEqualTo(model);
        assertThat(response.getResourceModel().getTags()).isEmpty();

        verify(proxyClient.client()).describeClusterSubnetGroups(any(DescribeClusterSubnetGroupsRequest.class));
        verify(proxyClient.client()).describeTags(any(DescribeTagsRequest.class));
    }

    @Test
    public void handleRequest_NotFound() {
        final ResourceModel model = BASIC_MODEL;

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(proxyClient.client().describeClusterSubnetGroups(any(DescribeClusterSubnetGroupsRequest.class)))
                .thenThrow(ClusterSubnetGroupNotFoundException.class);

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotFound);
    }

    @Test
    public void handleRequest_WithTags() {
        final ResourceModel model = MODEL_WITH_TAGS;

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(proxyClient.client().describeClusterSubnetGroups(any(DescribeClusterSubnetGroupsRequest.class)))
                .thenReturn(DescribeClusterSubnetGroupsResponse.builder()
                        .clusterSubnetGroups(Arrays.asList(BASIC_CLUSTER_SUBNET_GROUP))
                        .build());

        when(proxyClient.client().describeTags(any(DescribeTagsRequest.class)))
                .thenReturn(DESCRIBE_TAGS_RESPONSE);

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getResourceModel().getTags()).isNotNull();
        assertThat(response.getResourceModel().getTags()).isEqualTo(TAGS);
    }

    @Test
    public void handleRequest_InvalidTagResponse() {
        final ResourceModel model = BASIC_MODEL;

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(proxyClient.client().describeClusterSubnetGroups(any(DescribeClusterSubnetGroupsRequest.class)))
                .thenReturn(DescribeClusterSubnetGroupsResponse.builder()
                        .clusterSubnetGroups(Arrays.asList(BASIC_CLUSTER_SUBNET_GROUP))
                        .build());

        when(proxyClient.client().describeTags(any(DescribeTagsRequest.class)))
                .thenThrow(InvalidTagException.class);

        final ProgressEvent<ResourceModel, CallbackContext> response =
                handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.InvalidRequest);
    }
}