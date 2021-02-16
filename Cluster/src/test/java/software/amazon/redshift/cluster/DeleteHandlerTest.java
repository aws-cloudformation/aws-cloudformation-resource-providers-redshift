package software.amazon.redshift.cluster;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;

import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.Cluster;
import software.amazon.awssdk.services.redshift.model.ClusterNotFoundException;
import software.amazon.awssdk.services.redshift.model.DeleteClusterRequest;
import software.amazon.awssdk.services.redshift.model.DeleteClusterResponse;
import software.amazon.awssdk.services.redshift.model.DeleteClusterSubnetGroupRequest;
import software.amazon.awssdk.services.redshift.model.DeleteClusterSubnetGroupResponse;
import software.amazon.awssdk.services.redshift.model.DeleteTagsRequest;
import software.amazon.awssdk.services.redshift.model.DeleteTagsResponse;
import software.amazon.awssdk.services.redshift.model.DeleteUsageLimitRequest;
import software.amazon.awssdk.services.redshift.model.DeleteUsageLimitResponse;
import software.amazon.awssdk.services.redshift.model.DescribeClusterDbRevisionsResponse;
import software.amazon.awssdk.services.redshift.model.DescribeClustersRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClustersResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static software.amazon.redshift.cluster.TestUtils.AWS_REGION;
import static software.amazon.redshift.cluster.TestUtils.BASIC_CLUSTER_READ;
import static software.amazon.redshift.cluster.TestUtils.BASIC_MODEL;
import static software.amazon.redshift.cluster.TestUtils.CLUSTER_AVAILABLE;
import static software.amazon.redshift.cluster.TestUtils.CLUSTER_DB_REVISION;
import static software.amazon.redshift.cluster.TestUtils.CLUSTER_IDENTIFIER;
import static software.amazon.redshift.cluster.TestUtils.MASTER_USERNAME;
import static software.amazon.redshift.cluster.TestUtils.MASTER_USERPASSWORD;
import static software.amazon.redshift.cluster.TestUtils.NODETYPE;
import static software.amazon.redshift.cluster.TestUtils.NUMBER_OF_NODES;
import static software.amazon.redshift.cluster.TestUtils.RESOURCE_NAME;
import static software.amazon.redshift.cluster.TestUtils.USAGE_LIMIT_ID;
import static software.amazon.redshift.cluster.TestUtils.clusterEndpoint;

@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<RedshiftClient> proxyClient;

    @Mock
    RedshiftClient sdkClient;

    private DeleteHandler handler;

    @BeforeEach
    public void setup() {
        handler = new DeleteHandler();
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(RedshiftClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
    }

    @AfterEach
    public void tear_down() {
        verify(sdkClient, atLeastOnce()).serviceName();
        verifyNoMoreInteractions(sdkClient);
    }

    @Test
    public void handleRequest_SimpleSuccess() {

        final ResourceModel model = BASIC_MODEL;
        //model.setRedshiftCommand("delete-cluster");

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .region(AWS_REGION)
                .logicalResourceIdentifier("logicalId")
                .clientRequestToken("token")
                .snapshotRequested(false)
                .build();

        when(proxyClient.client().deleteCluster(any(DeleteClusterRequest.class)))
                .thenReturn(DeleteClusterResponse.builder().build());

        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class)))
                .thenThrow(ClusterNotFoundException.class);

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(null);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

//    @Test
//    public void testDeleteTags() {
//        List<String> tagKeys = new LinkedList<>();
//        tagKeys.add("K-sy");
//        final ResourceModel model = ResourceModel.builder()
//                .tagKeys(tagKeys)
//                .resourceName(RESOURCE_NAME)
//                .redshiftCommand("delete-tags")
//                .build();
//
//        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
//                .desiredResourceState(model)
//                .region(AWS_REGION)
//                .logicalResourceIdentifier("logicalId")
//                .clientRequestToken("token")
//                .build();
//
//        Cluster deleteTagsCLuster = Cluster.builder()
//                .clusterIdentifier(CLUSTER_IDENTIFIER)
//                .masterUsername(MASTER_USERNAME)
//                .nodeType("dc2.large")
//                .numberOfNodes(NUMBER_OF_NODES)
//                .clusterStatus(CLUSTER_AVAILABLE)
//                .publiclyAccessible(true)
//                .endpoint(clusterEndpoint)
//                .build();
//
//        when(proxyClient.client().deleteTags(any(DeleteTagsRequest.class)))
//                .thenReturn(DeleteTagsResponse.builder().build());
//
//        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class)))
//                .thenReturn(DescribeClustersResponse.builder()
//                        .clusters(deleteTagsCLuster)
//                        .build());
//
//        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
//
//        assertThat(response).isNotNull();
//        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
//        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
//        assertThat(response.getResourceModel()).isEqualTo(null);
//        assertThat(response.getResourceModels()).isNull();
//        assertThat(response.getMessage()).isNull();
//        assertThat(response.getErrorCode()).isNull();
//    }
//
//    @Test
//    public void testDeleteUsageLimit() {
//        final ResourceModel model = ResourceModel.builder()
//                .usageLimitId(USAGE_LIMIT_ID)
//                .redshiftCommand("delete-usage-limit")
//                .build();
//
//        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
//                .desiredResourceState(model)
//                .region(AWS_REGION)
//                .logicalResourceIdentifier("logicalId")
//                .clientRequestToken("token")
//                .build();
//
//        Cluster deleteUsageLimitCLuster = Cluster.builder()
//                .clusterIdentifier(CLUSTER_IDENTIFIER)
//                .masterUsername(MASTER_USERNAME)
//                .nodeType("dc2.large")
//                .numberOfNodes(NUMBER_OF_NODES)
//                .clusterStatus(CLUSTER_AVAILABLE)
//                .publiclyAccessible(true)
//                .endpoint(clusterEndpoint)
//                .build();
//
//        when(proxyClient.client().deleteUsageLimit(any(DeleteUsageLimitRequest.class)))
//                .thenReturn(DeleteUsageLimitResponse.builder().build());
//
//        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class)))
//                .thenReturn(DescribeClustersResponse.builder()
//                        .clusters(deleteUsageLimitCLuster)
//                        .build());
//
//        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
//
//        assertThat(response).isNotNull();
//        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
//        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
//        assertThat(response.getResourceModel()).isEqualTo(null);
//        assertThat(response.getResourceModels()).isNull();
//        assertThat(response.getMessage()).isNull();
//        assertThat(response.getErrorCode()).isNull();
//    }
}
