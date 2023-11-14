package software.amazon.redshift.cluster;

import java.time.Duration;
import java.util.LinkedList;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.*;
import software.amazon.awssdk.services.redshift.model.Endpoint;
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
        final ResourceModel model = createClusterResponseModel();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class))).thenReturn(describeClustersResponseSdk());
        when(proxyClient.client().describeLoggingStatus(any(DescribeLoggingStatusRequest.class))).thenReturn(describeLoggingStatusFalseResponseSdk());
        when(proxyClient.client().getResourcePolicy(any(GetResourcePolicyRequest.class))).thenReturn(getEmptyResourcePolicyResponseSdk());

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        response.getResourceModel().setLoggingProperties(LOGGING_PROPERTIES_DISABLED);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

    }

    @Test
    public void testClusterEndPoint() {
        Endpoint endpoint = Endpoint.builder()
                .port(1234)
                .address("read-cluster-endpoint.us-east-1.redshift.amazonaws.com")
                .build();

        final Cluster BASIC_CLUSTER = Cluster.builder()
                .clusterIdentifier("read-cluster-endpoint")
                .masterUsername("master")
                .nodeType("dc2.large")
                .numberOfNodes(2)
                .allowVersionUpgrade(true)
                .automatedSnapshotRetentionPeriod(0)
                .encrypted(false)
                .enhancedVpcRouting(false)
                .manualSnapshotRetentionPeriod(1)
                .publiclyAccessible(false)
                .clusterSecurityGroups(new LinkedList<ClusterSecurityGroupMembership>())
                .iamRoles(new LinkedList<ClusterIamRole>())
                .vpcSecurityGroups(new LinkedList<VpcSecurityGroupMembership>())
                .endpoint(endpoint)
                .build();
        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class)))
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(BASIC_CLUSTER)
                        .build());
        when(proxyClient.client().describeLoggingStatus(any(DescribeLoggingStatusRequest.class)))
                .thenReturn(DescribeLoggingStatusResponse.builder().loggingEnabled(false).build());
        when(proxyClient.client().getResourcePolicy(any(GetResourcePolicyRequest.class))).thenReturn(getEmptyResourcePolicyResponseSdk());


        software.amazon.redshift.cluster.Endpoint modelEndpoint = software.amazon.redshift.cluster.Endpoint.builder()
                .port("1234")
                .address("read-cluster-endpoint.us-east-1.redshift.amazonaws.com")
                .build();

        final ResourceModel BASIC_MODEL = ResourceModel.builder()
                .clusterIdentifier("read-cluster-endpoint")
                .masterUsername("master")
                .nodeType("dc2.large")
                .masterUserPassword(MASTER_USERPASSWORD)
                .numberOfNodes(2)
                .clusterType("multi-node")
                .allowVersionUpgrade(true)
                .automatedSnapshotRetentionPeriod(0)
                .encrypted(false)
                .publiclyAccessible(false)
                .clusterSecurityGroups(new LinkedList<String>())
                .iamRoles(new LinkedList<String>())
                .vpcSecurityGroupIds(new LinkedList<String>())
                .tags(new LinkedList<Tag>())
                .endpoint(modelEndpoint)
                .port(1234)
                .enhancedVpcRouting(false)
                .manualSnapshotRetentionPeriod(1)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(BASIC_MODEL)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        response.getResourceModel().setMasterUserPassword(MASTER_USERPASSWORD);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

    }
}
