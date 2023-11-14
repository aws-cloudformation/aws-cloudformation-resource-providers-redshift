package software.amazon.redshift.cluster;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.*;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static software.amazon.redshift.cluster.TestUtils.BASIC_CLUSTER;
import static software.amazon.redshift.cluster.TestUtils.BASIC_MODEL;
import static software.amazon.redshift.cluster.TestUtils.BASIC_RESOURCE_HANDLER_REQUEST;
import static software.amazon.redshift.cluster.TestUtils.MULTIAZ_CLUSTER;
import static software.amazon.redshift.cluster.TestUtils.modifyAttribute;

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
        handler = spy(new UpdateHandler());
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(RedshiftClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
        lenient().doNothing().when(handler).sleep(anyInt());
    }

    @AfterEach
    public void tear_down() {
        verify(sdkClient, atLeastOnce()).serviceName();
        verifyNoMoreInteractions(sdkClient);
    }

    @Test
    public void testDeleteNamespaceResourcePolicy() {
        ResourceModel previousModel = createClusterResponseModel();
        previousModel.setNamespaceResourcePolicy(Translator.convertStringToJson(NAMESPACE_POLICY, logger));

        ResourceModel updateModel = createClusterResponseModel();
        updateModel.setNamespaceResourcePolicy(Translator.convertStringToJson(NAMESPACE_POLICY_EMPTY, logger));

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(updateModel)
                .previousResourceState(previousModel)
                .awsAccountId(AWS_ACCOUNT_ID)
                .awsPartition(AWS_PARTITION)
                .region(AWS_REGION)
                .build();

        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class))).thenReturn(describeClustersResponseSdk());
        when(proxyClient.client().describeLoggingStatus(any(DescribeLoggingStatusRequest.class))).thenReturn(describeLoggingStatusFalseResponseSdk());
        when(proxyClient.client().getResourcePolicy(any(GetResourcePolicyRequest.class))).thenReturn(getEmptyResourcePolicyResponseSdk());
        when(proxyClient.client().deleteResourcePolicy(any(DeleteResourcePolicyRequest.class))).thenReturn(null);

        ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testModifyNamespaceResourcePolicy() {
        final String NEW_NAMESPACE_RESOURCE_POLICY = "{\"Version\":\"2012-10-17\"}";
        ResourceModel previousModel = createClusterResponseModel();
        previousModel.setNamespaceResourcePolicy(Translator.convertStringToJson(NAMESPACE_POLICY, logger));

        ResourceModel updateModel = createClusterResponseModel();
        updateModel.setNamespaceResourcePolicy(Translator.convertStringToJson(NEW_NAMESPACE_RESOURCE_POLICY, logger));

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(updateModel)
                .previousResourceState(previousModel)
                .awsAccountId(AWS_ACCOUNT_ID)
                .awsPartition(AWS_PARTITION)
                .region(AWS_REGION)
                .build();

        ResourcePolicy newResourcePolicy = ResourcePolicy.builder()
                .resourceArn(CLUSTER_NAMESPACE_ARN)
                .policy(NEW_NAMESPACE_RESOURCE_POLICY)
                .build();

        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class))).thenReturn(describeClustersResponseSdk());
        when(proxyClient.client().describeLoggingStatus(any(DescribeLoggingStatusRequest.class))).thenReturn(describeLoggingStatusFalseResponseSdk());

        when(proxyClient.client().putResourcePolicy(any(PutResourcePolicyRequest.class))).thenReturn(PutResourcePolicyResponse.builder()
                .resourcePolicy(newResourcePolicy)
                .build());
        when(proxyClient.client().getResourcePolicy(any(GetResourcePolicyRequest.class))).thenReturn(GetResourcePolicyResponse.builder()
                .resourcePolicy(newResourcePolicy)
                .build());

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        request.getDesiredResourceState().setMasterUserPassword(null);
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
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
        List<Tag> prevModelTags = Arrays.asList(tag, tagToRemove);

        final String roleToRemove = "arn:aws:iam::1111:role/cfn_migration_test_IAM_role_to_remove";
        List<String> prevModelIamRoles = Arrays.asList(IAM_ROLE_ARN, roleToRemove);

        LoggingProperties loggingProperties = LoggingProperties.builder()
                .bucketName(BUCKET_NAME)
                .s3KeyPrefix("test")
                .build();

        List<Tag> newModelTags = Arrays.asList(tag);
        List<String> newModelIamRoles = Arrays.asList(IAM_ROLE_ARN);

        ResourceModel previousModel = BASIC_MODEL.toBuilder()
                .nodeType("dc2.large")
                .numberOfNodes(NUMBER_OF_NODES)
                .iamRoles(prevModelIamRoles)
                .tags(prevModelTags)
                .loggingProperties(loggingProperties)
                .build();

        ResourceModel updateModel = BASIC_MODEL.toBuilder()
                .nodeType("dc2.large")
                .numberOfNodes(NUMBER_OF_NODES * 2)
                .iamRoles(newModelIamRoles)
                .tags(newModelTags)
                .loggingProperties(null)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = BASIC_RESOURCE_HANDLER_REQUEST.toBuilder()
                .awsPartition("aws-us-gov")
                .region("us-gov-west-1")
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
                .iamRoleArn(roleToRemove)
                .applyStatus("in-sync")
                .build();

        Cluster existingCluster = BASIC_CLUSTER.toBuilder()
                .numberOfNodes(NUMBER_OF_NODES)
                .iamRoles(iamRole, iamRoleRemove)
                .tags(software.amazon.awssdk.services.redshift.model.Tag.builder().key("foo").value("bar").build(),
                        software.amazon.awssdk.services.redshift.model.Tag.builder().key("foo-remove").value("bar-remove").build())
                .build();

        Cluster modifiedCluster_tagRemoved_iamRoleRemoved_loggingDisabled = BASIC_CLUSTER.toBuilder()
                .numberOfNodes(NUMBER_OF_NODES)
                .iamRoles(iamRole)
                .tags(software.amazon.awssdk.services.redshift.model.Tag.builder().key("foo").value("bar").build())
                .build();

        Cluster modifiedCluster_tagRemoved_iamRoleRemoved_loggingDisabled_ModifyNumberOfNodes = BASIC_CLUSTER.toBuilder()
                .numberOfNodes(NUMBER_OF_NODES*2)
                .iamRoles(iamRole)
                .tags(software.amazon.awssdk.services.redshift.model.Tag.builder().key("foo").value("bar").build())
                .build();

        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class)))
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(existingCluster)
                        .build())
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
                .thenReturn(DescribeLoggingStatusResponse.builder().loggingEnabled(true).build())
                .thenReturn(DescribeLoggingStatusResponse.builder().loggingEnabled(false).build());

        when(proxyClient.client().disableLogging(any(DisableLoggingRequest.class)))
                .thenReturn(DisableLoggingResponse.builder().build());

        when(proxyClient.client().resizeCluster(any(ResizeClusterRequest.class)))
                .thenReturn(ResizeClusterResponse.builder()
                        .cluster(modifiedCluster_tagRemoved_iamRoleRemoved_loggingDisabled_ModifyNumberOfNodes)
                        .build());
        when(proxyClient.client().getResourcePolicy(any(GetResourcePolicyRequest.class))).thenReturn(getEmptyResourcePolicyResponseSdk());


        ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        // verify that ARN is correctly constructed
        ArgumentCaptor<DeleteTagsRequest> deleteTagsRequestArgumentCaptor = ArgumentCaptor.forClass(DeleteTagsRequest.class);
        verify(proxyClient.client()).deleteTags(deleteTagsRequestArgumentCaptor.capture());
        final String govClusterArn = deleteTagsRequestArgumentCaptor.getValue().resourceName();
        assertThat(govClusterArn).isEqualTo("arn:aws-us-gov:redshift:us-gov-west-1:" + AWS_ACCOUNT_ID + ":cluster:" + CLUSTER_IDENTIFIER);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(30);

        response = handler.handleRequest(proxy, request, response.getCallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);

        assertThat(response.getResourceModel().getTags()).isEqualTo(request.getDesiredResourceState().getTags());
        assertThat(response.getResourceModel().getIamRoles()).isEqualTo(request.getDesiredResourceState().getIamRoles());
        assertThat(response.getResourceModel().getLoggingProperties().getBucketName()).isNull();
        assertThat(response.getResourceModel().getLoggingProperties().getS3KeyPrefix()).isNull();
        assertThat(response.getResourceModel().getNumberOfNodes()).isEqualTo(previousModel.getNumberOfNodes()*2);

        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void testCreateTags_CreateIamRole_Logging_ModifyNodeType() {
        List<Tag> prevTags = Arrays.asList(Tag.builder().key("dummyKey").value("dummyValue").build());
        List<String> prevModelIamRoles = Arrays.asList(IAM_ROLE_ARN);

        ResourceModel previousModel = BASIC_MODEL.toBuilder()
                .nodeType("dc2.large")
                .iamRoles(prevModelIamRoles)
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
        List<Tag> newModelTags = Arrays.asList(tag, tagToAdd);

        final String roleToAdd = "arn:aws:iam::1111:role/cfn_migration_test_IAM_role_to_add";
        List<String> newModelIamRoles = Arrays.asList(IAM_ROLE_ARN, roleToAdd);

        LoggingProperties loggingProperties = LoggingProperties.builder()
                .bucketName(BUCKET_NAME)
                .s3KeyPrefix("test/")
                .build();

        ResourceModel updateModel = BASIC_MODEL.toBuilder()
                .iamRoles(newModelIamRoles)
                .tags(newModelTags)
                .loggingProperties(loggingProperties)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = BASIC_RESOURCE_HANDLER_REQUEST.toBuilder()
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
                .iamRoleArn(roleToAdd)
                .applyStatus("in-sync")
                .build();

        Cluster existingCluster = BASIC_CLUSTER.toBuilder()
                .nodeType("dc2.large")
                .iamRoles(iamRole)
                .build();

        Cluster modifiedCluster_tagAdded_iamRoleAdded = BASIC_CLUSTER.toBuilder()
                .iamRoles(iamRole, iamRole_add)
                .tags(software.amazon.awssdk.services.redshift.model.Tag.builder().key("foo").value("bar").build(),
                        software.amazon.awssdk.services.redshift.model.Tag.builder().key("foo-add").value("bar-add").build())
                .build();

        Cluster modifiedCluster_tagAdded_iamRoleAdded_loggingEnabled_NodeTypeModify = BASIC_CLUSTER.toBuilder()
                .iamRoles(iamRole, iamRole_add)
                .tags(software.amazon.awssdk.services.redshift.model.Tag.builder().key("foo").value("bar").build(),
                        software.amazon.awssdk.services.redshift.model.Tag.builder().key("foo-add").value("bar-add").build())
                .build();

        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class)))
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
                .thenReturn(EnableLoggingResponse.builder().loggingEnabled(true).bucketName(BUCKET_NAME).s3KeyPrefix("test/").build());

        when(proxyClient.client().resizeCluster(any(ResizeClusterRequest.class)))
                .thenReturn(ResizeClusterResponse.builder()
                        .cluster(modifiedCluster_tagAdded_iamRoleAdded_loggingEnabled_NodeTypeModify)
                        .build());
        when(proxyClient.client().getResourcePolicy(any(GetResourcePolicyRequest.class))).thenReturn(getEmptyResourcePolicyResponseSdk());

        ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(30);

        when(proxyClient.client().describeLoggingStatus(any(DescribeLoggingStatusRequest.class)))
                .thenReturn(DescribeLoggingStatusResponse.builder()
                        .loggingEnabled(true)
                        .bucketName(BUCKET_NAME)
                        .s3KeyPrefix("test/")
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
    public void testModifyMasterUserPasswordAndPubliclyAccessible() {
        ResourceModel previousModel = BASIC_MODEL.toBuilder().build();

        ResourceModel updateModel = BASIC_MODEL.toBuilder()
                .masterUserPassword("new"+MASTER_USERPASSWORD)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = BASIC_RESOURCE_HANDLER_REQUEST.toBuilder()
                .desiredResourceState(updateModel)
                .previousResourceState(previousModel)
                .build();

        Cluster existingCluster = BASIC_CLUSTER.toBuilder().build();
        Cluster modifiedCluster = BASIC_CLUSTER.toBuilder().build();

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
        when(proxyClient.client().getResourcePolicy(any(GetResourcePolicyRequest.class))).thenReturn(getEmptyResourcePolicyResponseSdk());


        ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(30);

        response = handler.handleRequest(proxy, request, response.getCallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        verify(proxyClient.client()).modifyCluster(any(ModifyClusterRequest.class));

        // todo: make tests more independent so we can add tests like this elsewhere
        verify(handler).sleep(10);
        assertThat(UpdateHandler.DETECTABLE_MODIFY_CLUSTER_ATTRIBUTES_SENSITIVE.length == 1);
        assertThat(UpdateHandler.DETECTABLE_MODIFY_CLUSTER_ATTRIBUTES_INSENSITIVE.length == 17);

        assertThat(UpdateHandler.DETECTABLE_MODIFY_CLUSTER_ATTRIBUTES_SENSITIVE[0].equals("MasterUserPassword"));

        List<String> insensitiveFileds = Arrays.asList(
                "AllowVersionUpgrade",
                "AutomatedSnapshotRetentionPeriod",
                "AvailabilityZone",
                "AvailabilityZoneRelocation",
                "ClusterSecurityGroups",
                "ClusterVersion",
                "ElasticIp",
                "Encrypted",
                "EnhancedVpcRouting",
                "HsmClientCertificateIdentifier",
                "HsmConfigurationIdentifier",
                "KmsKeyId",
                "MaintenanceTrackName",
                "ManualSnapshotRetentionPeriod",
                "Port",
                "PreferredMaintenanceWindow",
                "PubliclyAccessible",
                "VpcSecurityGroupIds"
        );

        for (int i = 0; i < insensitiveFileds.size(); i++) {
            assertThat(UpdateHandler.DETECTABLE_MODIFY_CLUSTER_ATTRIBUTES_INSENSITIVE[i].equals(
                    insensitiveFileds.get(i)
            ));
        }
    }

    @Test
    public void testModifyEncrypted_EnableMultiAZ() {
        ResourceModel previousModel = BASIC_MODEL.toBuilder().build();

        ResourceModel updateModel = BASIC_MODEL.toBuilder()
                .encrypted(true)
                .multiAZ(true)
                .build();

        final ResourceHandlerRequest<ResourceModel> request = BASIC_RESOURCE_HANDLER_REQUEST.toBuilder()
                .desiredResourceState(updateModel)
                .previousResourceState(previousModel)
                .build();

        Cluster existingCluster = BASIC_CLUSTER.toBuilder().build();
        Cluster modifiedCluster = MULTIAZ_CLUSTER.toBuilder().build();

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
        when(proxyClient.client().getResourcePolicy(any(GetResourcePolicyRequest.class))).thenReturn(getEmptyResourcePolicyResponseSdk());


        ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(30);

        response = handler.handleRequest(proxy, request, response.getCallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        verify(proxyClient.client()).modifyCluster(any(ModifyClusterRequest.class));

        // todo: make tests more independent so we can add tests like this elsewhere
        verify(handler).sleep(10);
        assertThat(UpdateHandler.DETECTABLE_MODIFY_CLUSTER_ATTRIBUTES_SENSITIVE.length == 1);
        assertThat(UpdateHandler.DETECTABLE_MODIFY_CLUSTER_ATTRIBUTES_INSENSITIVE.length == 18);

        assertThat(UpdateHandler.DETECTABLE_MODIFY_CLUSTER_ATTRIBUTES_SENSITIVE[0].equals("Encrypted"));
        assertThat(UpdateHandler.DETECTABLE_MODIFY_CLUSTER_ATTRIBUTES_SENSITIVE[0].equals("MultiAZ"));

        List<String> insensitiveFileds = Arrays.asList(
                "AllowVersionUpgrade",
                "AutomatedSnapshotRetentionPeriod",
                "AvailabilityZone",
                "AvailabilityZoneRelocation",
                "ClusterSecurityGroups",
                "ClusterVersion",
                "ElasticIp",
                "Encrypted",
                "EnhancedVpcRouting",
                "HsmClientCertificateIdentifier",
                "HsmConfigurationIdentifier",
                "KmsKeyId",
                "MaintenanceTrackName",
                "ManualSnapshotRetentionPeriod",
                "Port",
                "PreferredMaintenanceWindow",
                "PubliclyAccessible",
                "VpcSecurityGroupIds",
                "MultiAZ"
        );

        for (int i = 0; i < insensitiveFileds.size(); i++) {
            assertThat(UpdateHandler.DETECTABLE_MODIFY_CLUSTER_ATTRIBUTES_INSENSITIVE[i].equals(
                    insensitiveFileds.get(i)
            ));
        }
    }

    private static boolean BOOLEAN_BEFORE = true;
    private static boolean BOOLEAN_AFTER = false;

    private static Stream<Arguments> detectableModifyClusterAttributeTest() {
        return Stream.of(
                Arguments.of("AllowVersionUpgrade", BOOLEAN_BEFORE, BOOLEAN_AFTER),
                Arguments.of("AutomatedSnapshotRetentionPeriod", 10, 15),
                Arguments.of("AvailabilityZone", "before-az", "after-az"),
                Arguments.of("AvailabilityZoneRelocation", true, false),
                // todo: mock this attribute properly
//                Arguments.of("ClusterSecurityGroups",
//                        Arrays.asList(ClusterSecurityGroupMembership.builder().clusterSecurityGroupName("before-sg").build().toString()),
//                        Arrays.asList(ClusterSecurityGroupMembership.builder().clusterSecurityGroupName("after-sg").build().toString())
//                ),
                Arguments.of("ClusterVersion", "before-cv", "after-cv"),
                Arguments.of("ElasticIp", "before-eip", "after-eip"),
                Arguments.of("Encrypted", BOOLEAN_BEFORE, BOOLEAN_AFTER),
                Arguments.of("EnhancedVpcRouting", BOOLEAN_BEFORE, BOOLEAN_AFTER),
                Arguments.of("HsmClientCertificateIdentifier", "before-hsm-cert-id", "after-hsm-cert-id"),
                Arguments.of("HsmConfigurationIdentifier", "before-hsm-config-id", "after-hsm-config-id"),
                Arguments.of("KmsKeyId", "before-kms", "after-kms"),
                Arguments.of("MaintenanceTrackName", "before-track", "after-track"),
                Arguments.of("ManualSnapshotRetentionPeriod", 5, 15),
                Arguments.of("MasterUserPassword", "before-password", "after-password"),
                Arguments.of("Port", 100, 200),
                Arguments.of("PreferredMaintenanceWindow", "before-window", "after-window"),
                Arguments.of("PubliclyAccessible", BOOLEAN_BEFORE, BOOLEAN_AFTER),
                Arguments.of("VpcSecurityGroupIds", Arrays.asList("before-sg-id"), Arrays.asList("after-sg-id"))
        );
    }

    @ParameterizedTest
    @MethodSource("detectableModifyClusterAttributeTest")
    public void testModifyClusterAttributes(
            String modifyClusterAttribute,
            Object beforeValue,
            Object afterValue
    ) throws Exception {
        if (modifyClusterAttribute.equals("ClusterSecurityGroups")) {
            return;
        }
        ResourceModel previousModel = BASIC_MODEL.toBuilder()
                .availabilityZoneRelocation(BOOLEAN_BEFORE)
                .masterUserPassword(MASTER_USERPASSWORD)
                .allowVersionUpgrade(BOOLEAN_BEFORE)
                .encrypted(BOOLEAN_BEFORE)
                .enhancedVpcRouting(BOOLEAN_BEFORE)
                .publiclyAccessible(BOOLEAN_BEFORE)
                .build();

        Cluster existingCluster = BASIC_CLUSTER.toBuilder()
                .allowVersionUpgrade(BOOLEAN_BEFORE)
                .encrypted(BOOLEAN_BEFORE)
                .enhancedVpcRouting(BOOLEAN_BEFORE)
                .publiclyAccessible(BOOLEAN_BEFORE)
                .build();

        String attributeName = Character.toLowerCase(modifyClusterAttribute.charAt(0)) + modifyClusterAttribute.substring(1);

        // copy modifiedCluster from existingCluster, then change one attribute
        Cluster modifiedCluster = existingCluster.toBuilder().build();
        modifyAttribute(existingCluster, Cluster.class, attributeName, beforeValue);
        modifyAttribute(modifiedCluster, Cluster.class, attributeName, afterValue);

        // copy from previousModel and modify one attribute
        ResourceModel updateModel = previousModel.toBuilder().build();
        modifyAttribute(updateModel, ResourceModel.class, attributeName, afterValue);

        final ResourceHandlerRequest<ResourceModel> request = BASIC_RESOURCE_HANDLER_REQUEST.toBuilder()
                .desiredResourceState(updateModel)
                .previousResourceState(previousModel)
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
        when(proxyClient.client().getResourcePolicy(any(GetResourcePolicyRequest.class))).thenReturn(getEmptyResourcePolicyResponseSdk());


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
