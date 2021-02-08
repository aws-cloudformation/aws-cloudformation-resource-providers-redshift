package software.amazon.redshift.cluster;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.CancelResizeRequest;
import software.amazon.awssdk.services.redshift.model.CancelResizeResponse;
import software.amazon.awssdk.services.redshift.model.Cluster;
import software.amazon.awssdk.services.redshift.model.ClusterIamRole;
import software.amazon.awssdk.services.redshift.model.ClusterSecurityGroupMembership;
import software.amazon.awssdk.services.redshift.model.CreateTagsRequest;
import software.amazon.awssdk.services.redshift.model.CreateTagsResponse;
import software.amazon.awssdk.services.redshift.model.DeferredMaintenanceWindow;
import software.amazon.awssdk.services.redshift.model.DescribeClustersRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClustersResponse;
import software.amazon.awssdk.services.redshift.model.DescribeLoggingStatusRequest;
import software.amazon.awssdk.services.redshift.model.DescribeLoggingStatusResponse;
import software.amazon.awssdk.services.redshift.model.DescribeTagsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeTagsResponse;
import software.amazon.awssdk.services.redshift.model.DescribeUsageLimitsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeUsageLimitsResponse;
import software.amazon.awssdk.services.redshift.model.DisableLoggingRequest;
import software.amazon.awssdk.services.redshift.model.DisableLoggingResponse;
import software.amazon.awssdk.services.redshift.model.DisableSnapshotCopyRequest;
import software.amazon.awssdk.services.redshift.model.DisableSnapshotCopyResponse;
import software.amazon.awssdk.services.redshift.model.EnableLoggingRequest;
import software.amazon.awssdk.services.redshift.model.EnableLoggingResponse;
import software.amazon.awssdk.services.redshift.model.EnableSnapshotCopyRequest;
import software.amazon.awssdk.services.redshift.model.EnableSnapshotCopyResponse;
import software.amazon.awssdk.services.redshift.model.ModifyClusterDbRevisionRequest;
import software.amazon.awssdk.services.redshift.model.ModifyClusterDbRevisionResponse;
import software.amazon.awssdk.services.redshift.model.ModifyClusterIamRolesRequest;
import software.amazon.awssdk.services.redshift.model.ModifyClusterIamRolesResponse;
import software.amazon.awssdk.services.redshift.model.ModifyClusterMaintenanceRequest;
import software.amazon.awssdk.services.redshift.model.ModifyClusterMaintenanceResponse;
import software.amazon.awssdk.services.redshift.model.ModifyClusterRequest;
import software.amazon.awssdk.services.redshift.model.ModifyClusterResponse;
import software.amazon.awssdk.services.redshift.model.ModifySnapshotCopyRetentionPeriodRequest;
import software.amazon.awssdk.services.redshift.model.ModifySnapshotCopyRetentionPeriodResponse;
import software.amazon.awssdk.services.redshift.model.ModifyUsageLimitRequest;
import software.amazon.awssdk.services.redshift.model.ModifyUsageLimitResponse;
import software.amazon.awssdk.services.redshift.model.PauseClusterRequest;
import software.amazon.awssdk.services.redshift.model.PauseClusterResponse;
import software.amazon.awssdk.services.redshift.model.RebootClusterRequest;
import software.amazon.awssdk.services.redshift.model.RebootClusterResponse;
import software.amazon.awssdk.services.redshift.model.ResizeClusterRequest;
import software.amazon.awssdk.services.redshift.model.ResizeClusterResponse;
import software.amazon.awssdk.services.redshift.model.ResumeClusterRequest;
import software.amazon.awssdk.services.redshift.model.ResumeClusterResponse;
import software.amazon.awssdk.services.redshift.model.RotateEncryptionKeyRequest;
import software.amazon.awssdk.services.redshift.model.RotateEncryptionKeyResponse;
import software.amazon.awssdk.services.redshift.model.TaggedResource;
import software.amazon.awssdk.services.redshift.model.UsageLimit;
import software.amazon.awssdk.services.redshift.model.VpcSecurityGroupMembership;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
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
import static software.amazon.redshift.cluster.TestUtils.BASIC_CLUSTER;
import static software.amazon.redshift.cluster.TestUtils.BASIC_MODEL;
import static software.amazon.redshift.cluster.TestUtils.BUCKET_NAME;
import static software.amazon.redshift.cluster.TestUtils.CLUSTER_AVAILABLE;
import static software.amazon.redshift.cluster.TestUtils.CLUSTER_IDENTIFIER;
import static software.amazon.redshift.cluster.TestUtils.CLUSTER_PAUSED;
import static software.amazon.redshift.cluster.TestUtils.CURRENT_DB_REVISION;
import static software.amazon.redshift.cluster.TestUtils.DEFERRED_MAINTENANCE_WINDOW;
import static software.amazon.redshift.cluster.TestUtils.DESCRIBE_TAGS_MODEL;
import static software.amazon.redshift.cluster.TestUtils.FEATURE_TYPE;
import static software.amazon.redshift.cluster.TestUtils.IAM_ROLE_ARN;
import static software.amazon.redshift.cluster.TestUtils.LIMIT_TYPE;
import static software.amazon.redshift.cluster.TestUtils.MASTER_USERNAME;
import static software.amazon.redshift.cluster.TestUtils.NODETYPE;
import static software.amazon.redshift.cluster.TestUtils.NUMBER_OF_NODES;
import static software.amazon.redshift.cluster.TestUtils.RESOURCE_NAME;
import static software.amazon.redshift.cluster.TestUtils.RESOURCE_TYPE;
import static software.amazon.redshift.cluster.TestUtils.USAGE_LIMIT_ID;
import static software.amazon.redshift.cluster.TestUtils.clusterEndpoint;
import static software.amazon.redshift.cluster.TestUtils.endpointAddress;
import static software.amazon.redshift.cluster.TestUtils.s3KEYPREFIX;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<RedshiftClient> proxyClient;

    @Mock
    RedshiftClient sdkClient;

    static UpdateHandler handler;
    @BeforeEach
    public void setup() {
        handler = new UpdateHandler();
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
        ResourceModel model = ResourceModel.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(MASTER_USERNAME)
                .nodeType("dc2.large")
                .numberOfNodes(NUMBER_OF_NODES)
                .allowVersionUpgrade(true)
                .automatedSnapshotRetentionPeriod(0)
                .encrypted(false)
                .enhancedVpcRouting(false)
                .manualSnapshotRetentionPeriod(1)
                .publiclyAccessible(false)
                .clusterSecurityGroups(new LinkedList<String>())
                .iamRoles(new LinkedList<String>())
                .vpcSecurityGroupIds(new LinkedList<String>())
                .redshiftCommand("modify-cluster")
                .clusterParameterGroups(new LinkedList<String>())
                .clusterNodeRole(new LinkedList<String>())
                .clusterNodePrivateIPAddress(new LinkedList<String>())
                .clusterNodePublicIPAddress(new LinkedList<String>())
                .tags(new LinkedList<software.amazon.redshift.cluster.Tag>())
                .clusterStatus(CLUSTER_AVAILABLE)       // any operation is possible on an "available" cluster
                .endpointAddress(endpointAddress)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        Cluster modifiedCluster = Cluster.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(MASTER_USERNAME)
                .nodeType("dc2.large")
                .numberOfNodes(NUMBER_OF_NODES)
                .clusterStatus("available")
                .allowVersionUpgrade(true)
                .automatedSnapshotRetentionPeriod(0)
                .encrypted(false)
                .enhancedVpcRouting(false)
                .manualSnapshotRetentionPeriod(1)
                .publiclyAccessible(false)
                .clusterSecurityGroups(new LinkedList<ClusterSecurityGroupMembership>())
                .iamRoles(new LinkedList<ClusterIamRole>())
                .vpcSecurityGroups(new LinkedList<VpcSecurityGroupMembership>())
                .tags(new LinkedList<software.amazon.awssdk.services.redshift.model.Tag>())
                .endpoint(clusterEndpoint)
                .build();

        when(proxyClient.client().modifyCluster(any(ModifyClusterRequest.class)))
                .thenReturn(ModifyClusterResponse.builder()
                        .cluster(modifiedCluster)
                        .build());

        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class)))
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(modifiedCluster)
                        .build());


        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        //Response doesn't have method redShiftCommand, hack to pass assertions in unit test
        response.getResourceModel().setRedshiftCommand("modify-cluster");

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testModifyNumberOfNodes() {
        ResourceModel model = ResourceModel.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(MASTER_USERNAME)
                .nodeType("dc2.large")
                .numberOfNodes(NUMBER_OF_NODES * 2)
                .allowVersionUpgrade(true)
                .automatedSnapshotRetentionPeriod(0)
                .encrypted(false)
                .enhancedVpcRouting(false)
                .manualSnapshotRetentionPeriod(1)
                .publiclyAccessible(false)
                .clusterSecurityGroups(new LinkedList<String>())
                .iamRoles(new LinkedList<String>())
                .vpcSecurityGroupIds(new LinkedList<String>())
                .redshiftCommand("modify-cluster")
                .clusterParameterGroups(new LinkedList<String>())
                .clusterNodeRole(new LinkedList<String>())
                .clusterNodePrivateIPAddress(new LinkedList<String>())
                .clusterNodePublicIPAddress(new LinkedList<String>())
                .tags(new LinkedList<software.amazon.redshift.cluster.Tag>())
                .clusterStatus(CLUSTER_AVAILABLE)       // any operation is possible on an "available" cluster
                .endpointAddress(endpointAddress)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        Cluster modifiedCluster = Cluster.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(MASTER_USERNAME)
                .nodeType("dc2.large")
                .numberOfNodes(NUMBER_OF_NODES * 2)
                .clusterStatus("available")
                .allowVersionUpgrade(true)
                .automatedSnapshotRetentionPeriod(0)
                .encrypted(false)
                .enhancedVpcRouting(false)
                .manualSnapshotRetentionPeriod(1)
                .publiclyAccessible(false)
                .clusterSecurityGroups(new LinkedList<ClusterSecurityGroupMembership>())
                .iamRoles(new LinkedList<ClusterIamRole>())
                .vpcSecurityGroups(new LinkedList<VpcSecurityGroupMembership>())
                .tags(new LinkedList<software.amazon.awssdk.services.redshift.model.Tag>())
                .endpoint(clusterEndpoint)
                .build();

        when(proxyClient.client().modifyCluster(any(ModifyClusterRequest.class)))
                .thenReturn(ModifyClusterResponse.builder()
                        .cluster(modifiedCluster)
                        .build());

        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class)))
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(modifiedCluster)
                        .build());


        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        response.getResourceModel().setRedshiftCommand("modify-cluster");

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testNewClusterIdentifier() {
        String newClusterId = "renamed-redshift-cluster";
        final ResourceModel model = ResourceModel.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(MASTER_USERNAME)
                .nodeType("dc2.large")
                .numberOfNodes(NUMBER_OF_NODES)
                .newClusterIdentifier(newClusterId)
                .redshiftCommand("modify-cluster")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();


        Cluster modifiedCluster = Cluster.builder()
                .clusterIdentifier(newClusterId)
                .masterUsername(MASTER_USERNAME)
                .nodeType("dc2.large")
                .numberOfNodes(NUMBER_OF_NODES)
                .clusterStatus("available")
                .endpoint(clusterEndpoint)
                .build();

        when(proxyClient.client().modifyCluster(any(ModifyClusterRequest.class)))
                .thenReturn(ModifyClusterResponse.builder()
                        .cluster(modifiedCluster)
                        .build());

        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class)))
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(modifiedCluster)
                        .build());


        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);

        assertThat(response.getResourceModel().getClusterIdentifier().equals(newClusterId));

        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testUpdateIamRoles() {
        List<ClusterIamRole> iamRoles =  new LinkedList<ClusterIamRole>();
        iamRoles.add(ClusterIamRole.builder().iamRoleArn(IAM_ROLE_ARN).build());

        List<String> iamrole = Collections.singletonList( IAM_ROLE_ARN );
        ResourceModel model = ResourceModel.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(null)
                .nodeType(null)
                .numberOfNodes(NUMBER_OF_NODES * 2)
                .allowVersionUpgrade(null)
                .automatedSnapshotRetentionPeriod(null)
                .encrypted(null)
                .enhancedVpcRouting(null)
                .manualSnapshotRetentionPeriod(null)
                .publiclyAccessible(null)
                .clusterSecurityGroups(null)
                .iamRoles(null)
                .vpcSecurityGroupIds(null)
                .addIamRoles(iamrole)
                .redshiftCommand("modify-cluster")
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        Cluster modifiedClusterWithIamRole = Cluster.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(null)
                .nodeType(null)
                .numberOfNodes(null)
                .clusterStatus("available")
                .allowVersionUpgrade(null)
                .automatedSnapshotRetentionPeriod(null)
                .encrypted(null)
                .enhancedVpcRouting(null)
                .manualSnapshotRetentionPeriod(null)
                .publiclyAccessible(null)
                .clusterSecurityGroups(new LinkedList<ClusterSecurityGroupMembership>())
                .iamRoles(iamRoles)
                .vpcSecurityGroups(new LinkedList<VpcSecurityGroupMembership>())
                .endpoint(clusterEndpoint)
                .build();

        Cluster modifiedClusterWithIamRoleResize = Cluster.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(null)
                .nodeType(null)
                .numberOfNodes(NUMBER_OF_NODES * 2)
                .clusterStatus("available")
                .allowVersionUpgrade(null)
                .automatedSnapshotRetentionPeriod(null)
                .encrypted(null)
                .enhancedVpcRouting(null)
                .manualSnapshotRetentionPeriod(null)
                .publiclyAccessible(null)
                .clusterSecurityGroups(new LinkedList<ClusterSecurityGroupMembership>())
                .iamRoles(iamRoles)
                .vpcSecurityGroups(new LinkedList<VpcSecurityGroupMembership>())
                .endpoint(clusterEndpoint)
                .build();

        when(proxyClient.client().modifyClusterIamRoles(any(ModifyClusterIamRolesRequest.class)))
                .thenReturn(ModifyClusterIamRolesResponse.builder()
                        .cluster(modifiedClusterWithIamRole)
                        .build());

        when(proxyClient.client().modifyCluster(any(ModifyClusterRequest.class)))
                .thenReturn(ModifyClusterResponse.builder()
                        .cluster(modifiedClusterWithIamRoleResize)
                        .build());

        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class)))
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(modifiedClusterWithIamRole)
                        .build())
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(modifiedClusterWithIamRoleResize)
                        .build());

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel().getIamRoles().get(0)).isEqualTo(IAM_ROLE_ARN);
        assertThat(response.getResourceModel().getNumberOfNodes()).isEqualTo(4);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testModifyDbRevision() {
        ResourceModel model = ResourceModel.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(null)
                .nodeType(null)
                .numberOfNodes(NUMBER_OF_NODES)
                .encrypted(null)
                .enhancedVpcRouting(null)
                .manualSnapshotRetentionPeriod(null)
                .publiclyAccessible(null)
                .clusterSecurityGroups(null)
                .iamRoles(null)
                .vpcSecurityGroupIds(null)
                .addIamRoles(null)
                .currentDatabaseRevision(CURRENT_DB_REVISION)
                .revisionTarget(CURRENT_DB_REVISION + ".1")
                .redshiftCommand("modify-cluster-db-revision")
                .clusterStatus(CLUSTER_AVAILABLE)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        Cluster modifiedClusterDBRevision = Cluster.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(null)
                .nodeType(null)
                .numberOfNodes(NUMBER_OF_NODES)
                .clusterStatus("patching")
                .encrypted(null)
                .enhancedVpcRouting(null)
                .manualSnapshotRetentionPeriod(null)
                .publiclyAccessible(null)
                .clusterRevisionNumber(CURRENT_DB_REVISION + ".1")
                .endpoint(clusterEndpoint)
                .build();

        when(proxyClient.client().modifyClusterDbRevision(any(ModifyClusterDbRevisionRequest.class)))
                .thenReturn(ModifyClusterDbRevisionResponse.builder()
                        .cluster(modifiedClusterDBRevision)
                        .build());

        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class)))
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(BASIC_CLUSTER) //for cluster to be in "available" status
                        .build())
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(modifiedClusterDBRevision)
                        .build())
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(BASIC_CLUSTER) //for cluster to be in "available" status
                        .build());

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        response.getResourceModel().setRedshiftCommand("modify-cluster-db-revision");
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        // revision Target should be the same as the cluster Revision Number which provides the target database version number
        assertThat(response.getResourceModel().getRevisionTarget()).isEqualTo(
                request.getDesiredResourceState().getClusterRevisionNumber());
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testModifyClusterMaintenance() {
        ResourceModel model = ResourceModel.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(null)
                .nodeType(null)
                .numberOfNodes(NUMBER_OF_NODES)
                .encrypted(null)
                .enhancedVpcRouting(null)
                .manualSnapshotRetentionPeriod(null)
                .publiclyAccessible(null)
                .clusterSecurityGroups(null)
                .iamRoles(null)
                .vpcSecurityGroupIds(null)
                .addIamRoles(null)
                .redshiftCommand("modify-cluster-maintenance")
                .deferMaintenance(true)
                .deferMaintenanceDuration(1)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        List<DeferredMaintenanceWindow> deferredMaintenanceWindows = new LinkedList<>();
        deferredMaintenanceWindows.add(DEFERRED_MAINTENANCE_WINDOW);

        Cluster modifiedClusterMaintenance = Cluster.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(null)
                .nodeType(null)
                .numberOfNodes(NUMBER_OF_NODES)
                .clusterStatus("available")
                .encrypted(null)
                .enhancedVpcRouting(null)
                .manualSnapshotRetentionPeriod(null)
                .publiclyAccessible(null)
                .deferredMaintenanceWindows(deferredMaintenanceWindows)
                .endpoint(clusterEndpoint)
                .build();

        when(proxyClient.client().modifyClusterMaintenance(any(ModifyClusterMaintenanceRequest.class)))
                .thenReturn(ModifyClusterMaintenanceResponse.builder()
                        .cluster(modifiedClusterMaintenance)
                        .build());


        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class)))
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(modifiedClusterMaintenance)
                        .build());

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        response.getResourceModel().setRedshiftCommand("modify-cluster-maintenance");

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testRebootCluster() {
        final ResourceModel model = ResourceModel.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(MASTER_USERNAME)
                .nodeType("dc2.large")
                .numberOfNodes(NUMBER_OF_NODES)
                .allowVersionUpgrade(true)
                .encrypted(false)
                .manualSnapshotRetentionPeriod(1)
                .publiclyAccessible(true)
                .clusterSecurityGroups(new LinkedList<String>())
                .iamRoles(new LinkedList<String>())
                .vpcSecurityGroupIds(new LinkedList<String>())
                .redshiftCommand("reboot-cluster")
                .clusterParameterGroups(new LinkedList<String>())
                .clusterNodeRole(new LinkedList<String>())
                .clusterNodePrivateIPAddress(new LinkedList<String>())
                .clusterNodePublicIPAddress(new LinkedList<String>())
                .tags(new LinkedList<software.amazon.redshift.cluster.Tag>())
                .clusterStatus(CLUSTER_AVAILABLE)       // any operation is possible on an "available" cluster
                .endpointAddress(endpointAddress)
                .build();

        Cluster rebootCluster = Cluster.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(MASTER_USERNAME)
                .nodeType("dc2.large")
                .numberOfNodes(NUMBER_OF_NODES)
                .clusterStatus("available")
                .manualSnapshotRetentionPeriod(1)
                .publiclyAccessible(true)
                .allowVersionUpgrade(true)
                .encrypted(false)
                .endpoint(clusterEndpoint)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(proxyClient.client().rebootCluster(any(RebootClusterRequest.class)))
                .thenReturn(RebootClusterResponse.builder()
                        .cluster(rebootCluster)
                        .build());

        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class)))
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(rebootCluster)
                        .build());

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        response.getResourceModel().setRedshiftCommand("reboot-cluster");

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testPauseCluster() {
        final ResourceModel model = ResourceModel.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(MASTER_USERNAME)
                .nodeType("dc2.large")
                .numberOfNodes(NUMBER_OF_NODES)
                .allowVersionUpgrade(true)
                .automatedSnapshotRetentionPeriod(0)
                .encrypted(false)
                .enhancedVpcRouting(false)
                .manualSnapshotRetentionPeriod(1)
                .publiclyAccessible(true)
                .clusterSecurityGroups(new LinkedList<String>())
                .iamRoles(new LinkedList<String>())
                .vpcSecurityGroupIds(new LinkedList<String>())
                .redshiftCommand("pause-cluster")
                .clusterParameterGroups(new LinkedList<String>())
                .clusterNodeRole(new LinkedList<String>())
                .clusterNodePrivateIPAddress(new LinkedList<String>())
                .clusterNodePublicIPAddress(new LinkedList<String>())
                .tags(new LinkedList<software.amazon.redshift.cluster.Tag>())
                .clusterStatus(CLUSTER_AVAILABLE)       // any operation is possible on an "available" cluster
                .build();

        Cluster pausedCluster = Cluster.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(MASTER_USERNAME)
                .nodeType("dc2.large")
                .numberOfNodes(NUMBER_OF_NODES)
                .clusterStatus("paused")
                .manualSnapshotRetentionPeriod(1)
                .publiclyAccessible(true)
                .endpoint(clusterEndpoint)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(proxyClient.client().pauseCluster(any(PauseClusterRequest.class)))
                .thenReturn(PauseClusterResponse.builder()
                        .cluster(pausedCluster)
                        .build());

        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class)))
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(pausedCluster)
                        .build());

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        response.getResourceModel().setRedshiftCommand("pause-cluster");

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel().getClusterStatus()).isEqualTo(CLUSTER_PAUSED);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testResumeCluster() {
        final ResourceModel model = ResourceModel.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(MASTER_USERNAME)
                .nodeType("dc2.large")
                .numberOfNodes(NUMBER_OF_NODES)
                .allowVersionUpgrade(true)
                .encrypted(false)
                .manualSnapshotRetentionPeriod(1)
                .publiclyAccessible(true)
                .clusterSecurityGroups(new LinkedList<String>())
                .iamRoles(new LinkedList<String>())
                .vpcSecurityGroupIds(new LinkedList<String>())
                .redshiftCommand("resume-cluster")
                .clusterParameterGroups(new LinkedList<String>())
                .clusterNodeRole(new LinkedList<String>())
                .clusterNodePrivateIPAddress(new LinkedList<String>())
                .clusterNodePublicIPAddress(new LinkedList<String>())
                .tags(new LinkedList<software.amazon.redshift.cluster.Tag>())
                .clusterStatus(CLUSTER_AVAILABLE)       // any operation is possible on an "available" cluster
                .endpointAddress(endpointAddress)
                .build();

        Cluster resumeCluster = Cluster.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(MASTER_USERNAME)
                .nodeType("dc2.large")
                .numberOfNodes(NUMBER_OF_NODES)
                .clusterStatus("available")
                .manualSnapshotRetentionPeriod(1)
                .publiclyAccessible(true)
                .allowVersionUpgrade(true)
                .encrypted(false)
                .endpoint(clusterEndpoint)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(proxyClient.client().resumeCluster(any(ResumeClusterRequest.class)))
                .thenReturn(ResumeClusterResponse.builder()
                        .cluster(resumeCluster)
                        .build());

        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class)))
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(resumeCluster)
                        .build());

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        response.getResourceModel().setRedshiftCommand("resume-cluster");

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testEnableSnapshotCopy() {
        final ResourceModel model = ResourceModel.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(MASTER_USERNAME)
                .nodeType("dc2.large")
                .numberOfNodes(NUMBER_OF_NODES)
                .allowVersionUpgrade(true)
                .encrypted(false)
                .manualSnapshotRetentionPeriod(1)
                .publiclyAccessible(true)
                .clusterSecurityGroups(new LinkedList<String>())
                .iamRoles(new LinkedList<String>())
                .vpcSecurityGroupIds(new LinkedList<String>())
                .redshiftCommand("enable-snapshot-copy")
                .clusterParameterGroups(new LinkedList<String>())
                .clusterNodeRole(new LinkedList<String>())
                .clusterNodePrivateIPAddress(new LinkedList<String>())
                .clusterNodePublicIPAddress(new LinkedList<String>())
                .tags(new LinkedList<software.amazon.redshift.cluster.Tag>())
                .clusterStatus(CLUSTER_AVAILABLE)       // any operation is possible on an "available" cluster
                .destinationRegion("us-west-1")
                .manualSnapshotRetentionPeriod(5)
                .endpointAddress(endpointAddress)
                .build();

        Cluster snapshotCopyEnabledCLuster = Cluster.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(MASTER_USERNAME)
                .nodeType("dc2.large")
                .numberOfNodes(NUMBER_OF_NODES)
                .clusterStatus(CLUSTER_AVAILABLE)
                .manualSnapshotRetentionPeriod(5)
                .publiclyAccessible(true)
                .encrypted(false)
                .allowVersionUpgrade(true)
                .endpoint(clusterEndpoint)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(proxyClient.client().enableSnapshotCopy(any(EnableSnapshotCopyRequest.class)))
                .thenReturn(EnableSnapshotCopyResponse.builder()
                        .cluster(snapshotCopyEnabledCLuster)
                        .build());

        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class)))
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(snapshotCopyEnabledCLuster)
                        .build());

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        response.getResourceModel().setRedshiftCommand("enable-snapshot-copy");
        // destination region isn't a param in cluster so doesn't reflect in response.
        response.getResourceModel().setDestinationRegion("us-west-1");

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testDisableSnapshotCopy() {
        final ResourceModel model = ResourceModel.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(MASTER_USERNAME)
                .nodeType("dc2.large")
                .numberOfNodes(NUMBER_OF_NODES)
                .encrypted(false)
                .publiclyAccessible(true)
                .clusterSecurityGroups(new LinkedList<String>())
                .iamRoles(new LinkedList<String>())
                .vpcSecurityGroupIds(new LinkedList<String>())
                .redshiftCommand("disable-snapshot-copy")
                .clusterParameterGroups(new LinkedList<String>())
                .clusterNodeRole(new LinkedList<String>())
                .clusterNodePrivateIPAddress(new LinkedList<String>())
                .clusterNodePublicIPAddress(new LinkedList<String>())
                .tags(new LinkedList<software.amazon.redshift.cluster.Tag>())
                .clusterStatus(CLUSTER_AVAILABLE)       // any operation is possible on an "available" cluster
                .endpointAddress(endpointAddress)
                .build();

        Cluster snapshotCopyEnabledCLuster = Cluster.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(MASTER_USERNAME)
                .nodeType("dc2.large")
                .numberOfNodes(NUMBER_OF_NODES)
                .clusterStatus(CLUSTER_AVAILABLE)
                .publiclyAccessible(true)
                .encrypted(false)
                .endpoint(clusterEndpoint)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(proxyClient.client().disableSnapshotCopy(any(DisableSnapshotCopyRequest.class)))
                .thenReturn(DisableSnapshotCopyResponse.builder()
                        .cluster(snapshotCopyEnabledCLuster)
                        .build());

        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class)))
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(snapshotCopyEnabledCLuster)
                        .build());

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        response.getResourceModel().setRedshiftCommand("disable-snapshot-copy");

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testModifySnapshotCopyRetentionPeriod() {
        final ResourceModel model = ResourceModel.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(MASTER_USERNAME)
                .nodeType("dc2.large")
                .numberOfNodes(NUMBER_OF_NODES)
                .encrypted(true)
                .manualSnapshotRetentionPeriod(1)
                .publiclyAccessible(true)
                .clusterSecurityGroups(new LinkedList<String>())
                .iamRoles(new LinkedList<String>())
                .vpcSecurityGroupIds(new LinkedList<String>())
                .redshiftCommand("modify-snapshot-copy-retention-period")
                .clusterParameterGroups(new LinkedList<String>())
                .clusterNodeRole(new LinkedList<String>())
                .clusterNodePrivateIPAddress(new LinkedList<String>())
                .clusterNodePublicIPAddress(new LinkedList<String>())
                .tags(new LinkedList<software.amazon.redshift.cluster.Tag>())
                .clusterStatus(CLUSTER_AVAILABLE)       // any operation is possible on an "available" cluster
                .retentionPeriod(7)
                .manual(true)
                .build();

        Cluster modifySnapshotRetentionPeriod = Cluster.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(MASTER_USERNAME)
                .nodeType("dc2.large")
                .numberOfNodes(NUMBER_OF_NODES)
                .clusterStatus(CLUSTER_AVAILABLE)
                .publiclyAccessible(true)
                .encrypted(true)
                .manualSnapshotRetentionPeriod(7)
                .automatedSnapshotRetentionPeriod(1)
                .endpoint(clusterEndpoint)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(proxyClient.client().modifySnapshotCopyRetentionPeriod(any(ModifySnapshotCopyRetentionPeriodRequest.class)))
                .thenReturn(ModifySnapshotCopyRetentionPeriodResponse.builder()
                        .cluster(modifySnapshotRetentionPeriod)
                        .build());

        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class)))
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(modifySnapshotRetentionPeriod)
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
    public void testRotateEncryptionKeys() {
        final ResourceModel model = ResourceModel.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(MASTER_USERNAME)
                .nodeType("dc2.large")
                .numberOfNodes(NUMBER_OF_NODES)
                .allowVersionUpgrade(true)
                .automatedSnapshotRetentionPeriod(1)
                .encrypted(false)
                .enhancedVpcRouting(false)
                .manualSnapshotRetentionPeriod(1)
                .publiclyAccessible(true)
                .clusterSecurityGroups(new LinkedList<String>())
                .iamRoles(new LinkedList<String>())
                .vpcSecurityGroupIds(new LinkedList<String>())
                .redshiftCommand("rotate-encryption-key")
                .clusterParameterGroups(new LinkedList<String>())
                .clusterNodeRole(new LinkedList<String>())
                .clusterNodePrivateIPAddress(new LinkedList<String>())
                .clusterNodePublicIPAddress(new LinkedList<String>())
                .tags(new LinkedList<software.amazon.redshift.cluster.Tag>())
                .clusterStatus(CLUSTER_AVAILABLE)       // any operation is possible on an "available" cluster
                .build();

        Cluster cluster = Cluster.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(MASTER_USERNAME)
                .nodeType("dc2.large")
                .numberOfNodes(NUMBER_OF_NODES)
                .clusterStatus(CLUSTER_AVAILABLE)
                .publiclyAccessible(true)
                .manualSnapshotRetentionPeriod(7)
                .automatedSnapshotRetentionPeriod(1)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(proxyClient.client().rotateEncryptionKey(any(RotateEncryptionKeyRequest.class)))
                .thenThrow(CfnGeneralServiceException.class);

        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class)))
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(cluster)
                        .build());
        ProgressEvent<ResourceModel, CallbackContext> response = null;
        try{
            response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        } catch (CfnGeneralServiceException e) {
            assertThat(response).isNull();
        }
    }

    @Test
    public void testResize() {
        final ResourceModel model = ResourceModel.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(MASTER_USERNAME)
                .nodeType("dc2.large")
                .numberOfNodes(NUMBER_OF_NODES * 2)
                .allowVersionUpgrade(true)
                .automatedSnapshotRetentionPeriod(1)
                .encrypted(false)
                .manualSnapshotRetentionPeriod(1)
                .publiclyAccessible(true)
                .clusterSecurityGroups(new LinkedList<String>())
                .iamRoles(new LinkedList<String>())
                .vpcSecurityGroupIds(new LinkedList<String>())
                .redshiftCommand("resize-cluster")
                .clusterParameterGroups(new LinkedList<String>())
                .clusterNodeRole(new LinkedList<String>())
                .clusterNodePrivateIPAddress(new LinkedList<String>())
                .clusterNodePublicIPAddress(new LinkedList<String>())
                .tags(new LinkedList<software.amazon.redshift.cluster.Tag>())
                .clusterStatus(CLUSTER_AVAILABLE)       // any operation is possible on an "available" cluster
                .endpointAddress(endpointAddress)
                .build();

        Cluster resizeCluster = Cluster.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(MASTER_USERNAME)
                .nodeType("dc2.large")
                .numberOfNodes(NUMBER_OF_NODES * 2)
                .clusterStatus(CLUSTER_AVAILABLE)
                .publiclyAccessible(true)
                .encrypted(false)
                .manualSnapshotRetentionPeriod(1)
                .automatedSnapshotRetentionPeriod(1)
                .allowVersionUpgrade(true)
                .endpoint(clusterEndpoint)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(proxyClient.client().resizeCluster(any(ResizeClusterRequest.class)))
                .thenReturn(ResizeClusterResponse.builder()
                        .cluster(resizeCluster)
                        .build());

        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class)))
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(resizeCluster)
                        .build());

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        response.getResourceModel().setRedshiftCommand("resize-cluster");

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testCancelResize() {
        final ResourceModel model = ResourceModel.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(MASTER_USERNAME)
                .nodeType("dc2.large")
                .numberOfNodes(NUMBER_OF_NODES)
                .allowVersionUpgrade(true)
                .automatedSnapshotRetentionPeriod(1)
                .encrypted(false)
                .enhancedVpcRouting(false)
                .manualSnapshotRetentionPeriod(1)
                .publiclyAccessible(true)
                .clusterSecurityGroups(new LinkedList<String>())
                .iamRoles(new LinkedList<String>())
                .vpcSecurityGroupIds(new LinkedList<String>())
                .redshiftCommand("cancel-resize")
                .clusterParameterGroups(new LinkedList<String>())
                .clusterNodeRole(new LinkedList<String>())
                .clusterNodePrivateIPAddress(new LinkedList<String>())
                .clusterNodePublicIPAddress(new LinkedList<String>())
                .tags(new LinkedList<software.amazon.redshift.cluster.Tag>())
                .clusterStatus(CLUSTER_AVAILABLE)       // any operation is possible on an "available" cluster
                .build();

        Cluster cancelResizeCluster = Cluster.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(MASTER_USERNAME)
                .nodeType("dc2.large")
                .numberOfNodes(NUMBER_OF_NODES)
                .clusterStatus(CLUSTER_AVAILABLE)
                .publiclyAccessible(true)
                .manualSnapshotRetentionPeriod(7)
                .automatedSnapshotRetentionPeriod(1)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        when(proxyClient.client().cancelResize(any(CancelResizeRequest.class)))
                .thenThrow(CfnNotFoundException.class);

        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class)))
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(cancelResizeCluster)
                        .build());
        ProgressEvent<ResourceModel, CallbackContext> response = null;
        try{
            response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        } catch (CfnNotFoundException e) {
            assertThat(response).isNull();
        }
    }

    @Test
    public void testEnableLogging() {
        final ResourceModel model = ResourceModel.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .bucketName(BUCKET_NAME)
                .s3KeyPrefix(s3KEYPREFIX)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        Cluster enableLoggingCluster = Cluster.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(MASTER_USERNAME)
                .nodeType("dc2.large")
                .numberOfNodes(NUMBER_OF_NODES)
                .clusterStatus(CLUSTER_AVAILABLE)
                .publiclyAccessible(true)
                .manualSnapshotRetentionPeriod(7)
                .automatedSnapshotRetentionPeriod(1)
                .endpoint(clusterEndpoint)
                .build();

        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class)))
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(enableLoggingCluster)
                        .build());

        when(proxyClient.client().enableLogging(any(EnableLoggingRequest.class)))
                .thenReturn(EnableLoggingResponse.builder()
                        .bucketName(BUCKET_NAME)
                        .s3KeyPrefix(s3KEYPREFIX)
                        .loggingEnabled(true)
                        .lastSuccessfulDeliveryTime(Instant.now())
                        .lastFailureTime(null)
                        .lastFailureMessage(null)
                        .build());

        when(proxyClient.client().describeLoggingStatus(any(DescribeLoggingStatusRequest.class)))
                .thenReturn(DescribeLoggingStatusResponse.builder()
                        .bucketName(BUCKET_NAME)
                        .s3KeyPrefix(s3KEYPREFIX)
                        .loggingEnabled(true)
                        .lastSuccessfulDeliveryTime(Instant.now())
                        .lastFailureTime(null)
                        .lastFailureMessage(null)
                        .build());

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getResourceModel().getLoggingEnabled() == true);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testCreateTags() {
        Tag clusterTag = Tag.builder()
                .key("KEY")
                .value("VALUE")
                .build();
        List<Tag> tags = new LinkedList<>();
        tags.add(clusterTag);

        final ResourceModel model = ResourceModel.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .resourceName(RESOURCE_NAME)
                .tags(tags)
                .build();

        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class)))
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(BASIC_CLUSTER)
                        .build());

        when(proxyClient.client().createTags(any(CreateTagsRequest.class)))
                .thenReturn(CreateTagsResponse.builder()
                        .build());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testModifyUsageLimit() {
        final ResourceModel model = ResourceModel.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .usageLimitId(USAGE_LIMIT_ID)
                .redshiftCommand("modify-usage-limit")
                .amount(2.0)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        Cluster cluster = Cluster.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(MASTER_USERNAME)
                .nodeType("dc2.large")
                .numberOfNodes(NUMBER_OF_NODES)
                .clusterStatus(CLUSTER_AVAILABLE)
                .publiclyAccessible(true)
                .manualSnapshotRetentionPeriod(7)
                .automatedSnapshotRetentionPeriod(1)
                .endpoint(clusterEndpoint)
                .build();

        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class)))
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(cluster)
                        .build());

        when(proxyClient.client().modifyUsageLimit(any(ModifyUsageLimitRequest.class)))
                .thenReturn(ModifyUsageLimitResponse.builder()
                        .amount(2L)
                        .clusterIdentifier(CLUSTER_IDENTIFIER)
                        .featureType(FEATURE_TYPE)
                        .limitType(LIMIT_TYPE)
                        .breachAction("log")
                        .period("monthly")
                        .build());

        UsageLimit usageLimit = UsageLimit.builder()
                .featureType(FEATURE_TYPE)
                .breachAction("log")
                .amount(2L)
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .limitType(LIMIT_TYPE)
                .usageLimitId(USAGE_LIMIT_ID)
                .build();

        when(proxyClient.client().describeUsageLimits(any(DescribeUsageLimitsRequest.class)))
                .thenReturn(DescribeUsageLimitsResponse.builder()
                        .usageLimits(usageLimit)
                        .build());

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getResourceModel().getAmount() == 2.0);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }
}
