package software.amazon.redshift.cluster;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.AquaConfiguration;
import software.amazon.awssdk.services.redshift.model.Cluster;
import software.amazon.awssdk.services.redshift.model.ClusterIamRole;
import software.amazon.awssdk.services.redshift.model.ClusterParameterGroupStatus;
import software.amazon.awssdk.services.redshift.model.ClusterSecurityGroupMembership;
import software.amazon.awssdk.services.redshift.model.ClusterSnapshotCopyStatus;
import software.amazon.awssdk.services.redshift.model.CreateClusterRequest;
import software.amazon.awssdk.services.redshift.model.CreateTagsRequest;
import software.amazon.awssdk.services.redshift.model.CreateTagsResponse;
import software.amazon.awssdk.services.redshift.model.DeleteTagsRequest;
import software.amazon.awssdk.services.redshift.model.DeleteTagsResponse;
import software.amazon.awssdk.services.redshift.model.DescribeClustersRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClustersResponse;
import software.amazon.awssdk.services.redshift.model.DescribeLoggingStatusRequest;
import software.amazon.awssdk.services.redshift.model.DescribeLoggingStatusResponse;
import software.amazon.awssdk.services.redshift.model.DescribeTagsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeTagsResponse;
import software.amazon.awssdk.services.redshift.model.DisableLoggingRequest;
import software.amazon.awssdk.services.redshift.model.DisableLoggingResponse;
import software.amazon.awssdk.services.redshift.model.EnableLoggingRequest;
import software.amazon.awssdk.services.redshift.model.EnableLoggingResponse;
import software.amazon.awssdk.services.redshift.model.ModifyClusterIamRolesRequest;
import software.amazon.awssdk.services.redshift.model.ModifyClusterIamRolesResponse;
import software.amazon.awssdk.services.redshift.model.ModifyClusterRequest;
import software.amazon.awssdk.services.redshift.model.ModifyClusterResponse;
import software.amazon.awssdk.services.redshift.model.ResizeClusterRequest;
import software.amazon.awssdk.services.redshift.model.ResizeClusterResponse;
import software.amazon.awssdk.services.redshift.model.TaggedResource;
import software.amazon.awssdk.services.redshift.model.VpcSecurityGroupMembership;
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
import static software.amazon.redshift.cluster.TestUtils.BASIC_CLUSTER;
import static software.amazon.redshift.cluster.TestUtils.BASIC_MODEL;
import static software.amazon.redshift.cluster.TestUtils.BUCKET_NAME;
import static software.amazon.redshift.cluster.TestUtils.CLUSTER_IDENTIFIER;
import static software.amazon.redshift.cluster.TestUtils.IAM_ROLE_ARN;
import static software.amazon.redshift.cluster.TestUtils.MASTER_USERNAME;
import static software.amazon.redshift.cluster.TestUtils.MASTER_USERPASSWORD;
import static software.amazon.redshift.cluster.TestUtils.NODETYPE;
import static software.amazon.redshift.cluster.TestUtils.NUMBER_OF_NODES;
import static software.amazon.redshift.cluster.TestUtils.OWNER_ACCOUNT_NO;
import static software.amazon.redshift.cluster.TestUtils.RESOURCE_NAME_PREFIX;

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
    public void testRemoveTags_RemoveIamRole_DisableLogging_ModifyNumOfNodes() {
        Tag tag = Tag.builder()
                .key("foo")
                .value("bar")
                .build();

        Tag tagToRemove = Tag.builder()
                .key("foo-remove")
                .value("bar-remove")
                .build();

        List<Tag> prevModelTags = new LinkedList<>();
        prevModelTags.add(tag);
        prevModelTags.add(tagToRemove);

        List<String> prevModelIamRoles = new LinkedList<>();
        prevModelIamRoles.add(IAM_ROLE_ARN);
        prevModelIamRoles.add("arn:aws:iam::1111:role/cfn_migration_test_IAM_role_to_remove");

        LoggingProperties loggingProperties = LoggingProperties.builder()
                .bucketName(BUCKET_NAME)
                .build();

        ResourceModel previousModel = ResourceModel.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(MASTER_USERNAME)
                .masterUserPassword(MASTER_USERPASSWORD)
                .nodeType("dc2.large")
                .clusterType("multi-node")
                .numberOfNodes(NUMBER_OF_NODES)
                .allowVersionUpgrade(true)
                .automatedSnapshotRetentionPeriod(0)
                .encrypted(false)
                .publiclyAccessible(false)
                .clusterSecurityGroups(new LinkedList<String>())
                .iamRoles(prevModelIamRoles)
                .vpcSecurityGroupIds(new LinkedList<String>())
                .tags(prevModelTags)
                .loggingProperties(loggingProperties)
                .build();


        List<Tag> newModelTags = new LinkedList<>();
        newModelTags.add(tag);

        List<String> newModelIamRoles = new LinkedList<>();
        newModelIamRoles.add(IAM_ROLE_ARN);

        ResourceModel updateModel = ResourceModel.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(MASTER_USERNAME)
                .masterUserPassword(MASTER_USERPASSWORD)
                .nodeType("dc2.large")
                .clusterType("multi-node")
                .numberOfNodes(NUMBER_OF_NODES * 2)
                .allowVersionUpgrade(true)
                .automatedSnapshotRetentionPeriod(0)
                .encrypted(false)
                .publiclyAccessible(false)
                .clusterSecurityGroups(new LinkedList<String>())
                .iamRoles(newModelIamRoles)
                .vpcSecurityGroupIds(new LinkedList<String>())
                .tags(newModelTags)
                .loggingProperties(null)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(updateModel)
                .previousResourceState(previousModel)
                .build();

        ClusterIamRole iamRole = ClusterIamRole
                .builder()
                .iamRoleArn(IAM_ROLE_ARN)
                .applyStatus("in-sync")
                .build();

        ClusterIamRole iamRoleRemove = ClusterIamRole
                .builder()
                .iamRoleArn("arn:aws:iam::1111:role/cfn_migration_test_IAM_role-remove")
                .applyStatus("in-sync")
                .build();

        Cluster existingCluster = Cluster.builder()
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
                .iamRoles(iamRole, iamRoleRemove)
                .vpcSecurityGroups(new LinkedList<VpcSecurityGroupMembership>())
                .tags(software.amazon.awssdk.services.redshift.model.Tag.builder().key("foo").value("bar").build(), software.amazon.awssdk.services.redshift.model.Tag.builder().key("foo-remove").value("bar-remove").build())
                .clusterSnapshotCopyStatus(ClusterSnapshotCopyStatus.builder().build())
                .build();

        Cluster modifiedCluster_tagRemoved_iamRoleRemoved_loggingDisabled = Cluster.builder()
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
                .iamRoles(iamRole)
                .vpcSecurityGroups(new LinkedList<VpcSecurityGroupMembership>())
                .tags(software.amazon.awssdk.services.redshift.model.Tag.builder().key("foo").value("bar").build())
                .clusterSnapshotCopyStatus(ClusterSnapshotCopyStatus.builder().build())
                .build();

        Cluster modifiedCluster_tagRemoved_iamRoleRemoved_loggingDisabled_ModifyNumberOfNodes = Cluster.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(MASTER_USERNAME)
                .nodeType("dc2.large")
                .numberOfNodes(NUMBER_OF_NODES*2)
                .clusterStatus("available")
                .allowVersionUpgrade(true)
                .automatedSnapshotRetentionPeriod(0)
                .encrypted(false)
                .enhancedVpcRouting(false)
                .manualSnapshotRetentionPeriod(1)
                .publiclyAccessible(false)
                .clusterSecurityGroups(new LinkedList<ClusterSecurityGroupMembership>())
                .iamRoles(iamRole)
                .vpcSecurityGroups(new LinkedList<VpcSecurityGroupMembership>())
                .tags(software.amazon.awssdk.services.redshift.model.Tag.builder().key("foo").value("bar").build())
                .clusterParameterGroups(ClusterParameterGroupStatus.builder().parameterApplyStatus("in-sync").build())
                .clusterSnapshotCopyStatus(ClusterSnapshotCopyStatus.builder().build())
                .build();

        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class)))
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(existingCluster)
                        .build())
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(existingCluster)
                        .build())
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(existingCluster)
                        .build())
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(existingCluster)
                        .build())
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(existingCluster)
                        .build())
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(existingCluster)
                        .build())
//                .thenReturn(DescribeClustersResponse.builder()
//                        .clusters(existingCluster)
//                        .build())
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(modifiedCluster_tagRemoved_iamRoleRemoved_loggingDisabled)
                        .build())
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(modifiedCluster_tagRemoved_iamRoleRemoved_loggingDisabled_ModifyNumberOfNodes)
                        .build());

        when(proxyClient.client().deleteTags(any(DeleteTagsRequest.class)))
                .thenReturn(DeleteTagsResponse.builder().build());

        when(proxyClient.client().modifyClusterIamRoles(any(ModifyClusterIamRolesRequest.class)))
                .thenReturn(ModifyClusterIamRolesResponse.builder()
                        .cluster(modifiedCluster_tagRemoved_iamRoleRemoved_loggingDisabled)
                        .build());

        when(proxyClient.client().describeLoggingStatus(any(DescribeLoggingStatusRequest.class)))
                .thenReturn(DescribeLoggingStatusResponse.builder().loggingEnabled(true).build());

        when(proxyClient.client().disableLogging(any(DisableLoggingRequest.class)))
                .thenReturn(DisableLoggingResponse.builder().build());

        when(proxyClient.client().resizeCluster(any(ResizeClusterRequest.class)))
                .thenReturn(ResizeClusterResponse.builder()
                        .cluster(modifiedCluster_tagRemoved_iamRoleRemoved_loggingDisabled_ModifyNumberOfNodes)
                        .build());


        ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(30);

        response = handler.handleRequest(proxy, request, response.getCallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);

        assertThat(response.getResourceModel().getTags()).isEqualTo(request.getDesiredResourceState().getTags());
        assertThat(response.getResourceModel().getIamRoles()).isEqualTo(request.getDesiredResourceState().getIamRoles());
        assertThat(response.getResourceModel().getLoggingProperties()).isNull();
        assertThat(response.getResourceModel().getNumberOfNodes()).isEqualTo(previousModel.getNumberOfNodes()*2);

        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testCreateTags_CreateIamRole_Logging_ModifyNodeType() {
        List<Tag> prevTags = new LinkedList<>();
        prevTags.add(Tag.builder().key("dummyKey").value("dummyValue").build());
        List<String> prevModelIamRoles = new LinkedList<>();
        prevModelIamRoles.add(IAM_ROLE_ARN);

        ResourceModel previousModel = ResourceModel.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(MASTER_USERNAME)
                .masterUserPassword(MASTER_USERPASSWORD)
                .nodeType("dc2.large")
                .clusterType("multi-node")
                .numberOfNodes(NUMBER_OF_NODES)
                .allowVersionUpgrade(true)
                .automatedSnapshotRetentionPeriod(0)
                .encrypted(false)
                .publiclyAccessible(false)
                .clusterSecurityGroups(new LinkedList<String>())
                .iamRoles(prevModelIamRoles)
                .vpcSecurityGroupIds(new LinkedList<String>())
                .tags(prevTags)
                .loggingProperties(null)
                .build();

        Tag tag = Tag.builder()
                .key("foo")
                .value("bar")
                .build();
        Tag tagToAdd = Tag.builder()
                .key("foo-add")
                .value("bar-add")
                .build();
        List<Tag> newModelTags = new LinkedList<>();
        newModelTags.add(tag);
        newModelTags.add(tagToAdd);

        List<String> newModelIamRoles = new LinkedList<>();
        newModelIamRoles.add(IAM_ROLE_ARN);
        newModelIamRoles.add("arn:aws:iam::1111:role/cfn_migration_test_IAM_role_to_add");

        LoggingProperties loggingProperties = LoggingProperties.builder()
                .bucketName(BUCKET_NAME)
                .build();

        ResourceModel updateModel = ResourceModel.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(MASTER_USERNAME)
                .masterUserPassword(MASTER_USERPASSWORD)
                .nodeType("ds2.xlarge")
                .clusterType("multi-node")
                .numberOfNodes(NUMBER_OF_NODES)
                .allowVersionUpgrade(true)
                .automatedSnapshotRetentionPeriod(0)
                .encrypted(false)
                .publiclyAccessible(false)
                .clusterSecurityGroups(new LinkedList<String>())
                .iamRoles(newModelIamRoles)
                .vpcSecurityGroupIds(new LinkedList<String>())
                .tags(newModelTags)
                .loggingProperties(loggingProperties)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(updateModel)
                .previousResourceState(previousModel)
                .build();

        ClusterIamRole iamRole = ClusterIamRole
                .builder()
                .iamRoleArn(IAM_ROLE_ARN)
                .applyStatus("in-sync")
                .build();
        ClusterIamRole iamRole_add = ClusterIamRole
                .builder()
                .iamRoleArn("arn:aws:iam::1111:role/cfn_migration_test_IAM_role_to_add")
                .applyStatus("in-sync")
                .build();

        Cluster existingCluster = Cluster.builder()
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
                .iamRoles(iamRole)
                .vpcSecurityGroups(new LinkedList<VpcSecurityGroupMembership>())
                .clusterSnapshotCopyStatus(ClusterSnapshotCopyStatus.builder().build())
                .build();


        Cluster modifiedCluster_tagAdded_iamRoleAdded = Cluster.builder()
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
                .iamRoles(iamRole, iamRole_add)
                .vpcSecurityGroups(new LinkedList<VpcSecurityGroupMembership>())
                .tags(software.amazon.awssdk.services.redshift.model.Tag.builder().key("foo").value("bar").build(), software.amazon.awssdk.services.redshift.model.Tag.builder().key("foo-add").value("bar-add").build())
                .clusterSnapshotCopyStatus(ClusterSnapshotCopyStatus.builder().build())
                .build();

        Cluster modifiedCluster_tagAdded_iamRoleAdded_loggingEnabled_NodeTypeModify = Cluster.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(MASTER_USERNAME)
                .nodeType("ds2.xlarge")
                .numberOfNodes(NUMBER_OF_NODES)
                .clusterStatus("available")
                .allowVersionUpgrade(true)
                .automatedSnapshotRetentionPeriod(0)
                .encrypted(false)
                .enhancedVpcRouting(false)
                .manualSnapshotRetentionPeriod(1)
                .publiclyAccessible(false)
                .clusterSecurityGroups(new LinkedList<ClusterSecurityGroupMembership>())
                .iamRoles(iamRole, iamRole_add)
                .vpcSecurityGroups(new LinkedList<VpcSecurityGroupMembership>())
                .tags(software.amazon.awssdk.services.redshift.model.Tag.builder().key("foo").value("bar").build(), software.amazon.awssdk.services.redshift.model.Tag.builder().key("foo-add").value("bar-add").build())
                .clusterParameterGroups(ClusterParameterGroupStatus.builder().parameterApplyStatus("in-sync").build())
                .clusterSnapshotCopyStatus(ClusterSnapshotCopyStatus.builder().build())
                .build();

        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class)))
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(existingCluster)
                        .build())
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(existingCluster)
                        .build())
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(existingCluster)
                        .build())
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(existingCluster)
                        .build())
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(existingCluster)
                        .build())
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(existingCluster)
                        .build())
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(existingCluster)
                        .build())
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(modifiedCluster_tagAdded_iamRoleAdded)
                        .build())
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(modifiedCluster_tagAdded_iamRoleAdded_loggingEnabled_NodeTypeModify)
                        .build());

        when(proxyClient.client().createTags(any(CreateTagsRequest.class)))
                .thenReturn(CreateTagsResponse.builder().build());

        when(proxyClient.client().deleteTags(any(DeleteTagsRequest.class)))
                .thenReturn(DeleteTagsResponse.builder().build());


        when(proxyClient.client().modifyClusterIamRoles(any(ModifyClusterIamRolesRequest.class)))
                .thenReturn(ModifyClusterIamRolesResponse.builder()
                        .cluster(modifiedCluster_tagAdded_iamRoleAdded)
                        .build());

        when(proxyClient.client().enableLogging(any(EnableLoggingRequest.class)))
                .thenReturn(EnableLoggingResponse.builder().loggingEnabled(true).bucketName(BUCKET_NAME).build());

        when(proxyClient.client().resizeCluster(any(ResizeClusterRequest.class)))
                .thenReturn(ResizeClusterResponse.builder()
                        .cluster(modifiedCluster_tagAdded_iamRoleAdded_loggingEnabled_NodeTypeModify)
                        .build());

        ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(30);

        when(proxyClient.client().describeLoggingStatus(any(DescribeLoggingStatusRequest.class)))
                .thenReturn(DescribeLoggingStatusResponse.builder()
                        .loggingEnabled(true)
                        .bucketName(BUCKET_NAME)
                        .build());
        //call back
        response = handler.handleRequest(proxy, request, response.getCallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);

        assertThat(response.getResourceModel().getTags()).isEqualTo(newModelTags);
        assertThat(response.getResourceModel().getIamRoles()).isEqualTo(newModelIamRoles);
        assertThat(response.getResourceModel().getLoggingProperties()).isEqualTo(loggingProperties);
        assertThat(response.getResourceModel().getNodeType()).isEqualTo(request.getDesiredResourceState().getNodeType());

        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testModifyMasterUserName() {
        ResourceModel previousModel = ResourceModel.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(MASTER_USERNAME)
                .masterUserPassword(MASTER_USERPASSWORD)
                .nodeType("dc2.large")
                .clusterType("multi-node")
                .numberOfNodes(NUMBER_OF_NODES)
                .allowVersionUpgrade(true)
                .automatedSnapshotRetentionPeriod(0)
                .encrypted(false)
                .publiclyAccessible(false)
                .clusterSecurityGroups(new LinkedList<String>())
                .iamRoles(null)
                .vpcSecurityGroupIds(new LinkedList<String>())
                .tags(null)
                .build();

        ResourceModel updateModel = ResourceModel.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(MASTER_USERNAME)
                .masterUserPassword("new"+MASTER_USERPASSWORD)
                .nodeType("dc2.large")
                .clusterType("multi-node")
                .numberOfNodes(NUMBER_OF_NODES)
                .allowVersionUpgrade(true)
                .automatedSnapshotRetentionPeriod(0)
                .encrypted(false)
                .publiclyAccessible(false)
                .clusterSecurityGroups(new LinkedList<String>())
                .iamRoles(null)
                .vpcSecurityGroupIds(new LinkedList<String>())
                .tags(null)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(updateModel)
                .previousResourceState(previousModel)
                .build();

        Cluster existingCluster = Cluster.builder()
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
                .clusterSnapshotCopyStatus(ClusterSnapshotCopyStatus.builder().build())
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
                .clusterSnapshotCopyStatus(ClusterSnapshotCopyStatus.builder().build())
                .build();

        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class)))
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(existingCluster)
                        .build())
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(modifiedCluster)
                        .build());

        when(proxyClient.client().describeLoggingStatus(any(DescribeLoggingStatusRequest.class)))
                .thenReturn(DescribeLoggingStatusResponse.builder().build());

        when(proxyClient.client().modifyCluster(any(ModifyClusterRequest.class)))
                .thenReturn(ModifyClusterResponse.builder()
                        .cluster(modifiedCluster)
                        .build());


        ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(30);

        response = handler.handleRequest(proxy, request, response.getCallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        verify(proxyClient.client()).modifyCluster(any(ModifyClusterRequest.class));
    }

}
