package software.amazon.redshift.cluster;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.Cluster;
import software.amazon.awssdk.services.redshift.model.CreateClusterRequest;
import software.amazon.awssdk.services.redshift.model.CreateClusterResponse;
import software.amazon.awssdk.services.redshift.model.DescribeClustersRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClustersResponse;
import software.amazon.awssdk.services.redshift.model.DescribeLoggingStatusRequest;
import software.amazon.awssdk.services.redshift.model.DescribeLoggingStatusResponse;
import software.amazon.awssdk.services.redshift.model.EnableLoggingRequest;
import software.amazon.awssdk.services.redshift.model.EnableLoggingResponse;
import software.amazon.awssdk.services.redshift.model.GetResourcePolicyRequest;
import software.amazon.awssdk.services.redshift.model.ModifyClusterMaintenanceRequest;
import software.amazon.awssdk.services.redshift.model.PutResourcePolicyRequest;
import software.amazon.awssdk.services.redshift.model.RestoreFromClusterSnapshotRequest;
import software.amazon.awssdk.services.redshift.model.RestoreFromClusterSnapshotResponse;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static software.amazon.redshift.cluster.TestUtils.MULTIAZ_CLUSTER;
import static software.amazon.redshift.cluster.TestUtils.MULTIAZ_ENABLED;
import static software.amazon.redshift.cluster.TestUtils.MANAGED_ADMIN_PASSWORD_CLUSTER;
import static software.amazon.redshift.cluster.TestUtils.MASTER_PASSWORD_SECRET_ARN;
import static software.amazon.redshift.cluster.TestUtils.MASTER_PASSWORD_SECRET_KMS_KEY_ID;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<RedshiftClient> proxyClient;

    @Mock
    RedshiftClient sdkClient;

    private CreateHandler handler;

    @BeforeEach
    public void setup() {
        handler = new CreateHandler();
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
        Tag tag = Tag.builder()
                .key("foo")
                .value("bar")
                .build();

        List<Tag> tags = new LinkedList<>();
        tags.add(tag);

        ResourceModel model = createClusterRequestModel();
        model.setTags(tags);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .region(AWS_REGION)
                .logicalResourceIdentifier("logicalId")
                .clientRequestToken("token")
                .build();

        when(proxyClient.client().createCluster(any(CreateClusterRequest.class))).thenReturn(createClusterResponseSdk());

        software.amazon.awssdk.services.redshift.model.Tag clusterTag = software.amazon.awssdk.services.redshift.model.Tag.builder()
                .key("foo")
                .value("bar")
                .build();

        List<software.amazon.awssdk.services.redshift.model.Tag> clusterTags = new LinkedList<>();
        clusterTags.add(clusterTag);

        Cluster clusterWithTags = responseCluster();
        clusterWithTags.toBuilder().tags(clusterTags).build();

        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class)))
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(clusterWithTags)
                        .build());

        when(proxyClient.client().describeLoggingStatus(any(DescribeLoggingStatusRequest.class)))
                .thenReturn(DescribeLoggingStatusResponse.builder().loggingEnabled(false).build());
        when(proxyClient.client().getResourcePolicy(any(GetResourcePolicyRequest.class))).thenReturn(getEmptyResourcePolicyResponseSdk());

        ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        response.getResourceModel().setMasterUserPassword(MASTER_USERPASSWORD);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(30);

        response = handler.handleRequest(proxy, request, response.getCallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getResourceModel().getClusterNamespaceArn()).isNotNull();
        assertThat(response.getResourceModel().getClusterIdentifier()).
                isEqualTo(request.getDesiredResourceState().getClusterIdentifier());

        verify(proxyClient.client()).createCluster(any(CreateClusterRequest.class));
        verify(proxyClient.client(), times(3))
                .describeClusters(any(DescribeClustersRequest.class));

    }

    @Test
    public void testCreateClusterAndEnableLogging() {
        ResourceModel model = createClusterRequestModel();
        model.setLoggingProperties(LOGGING_PROPERTIES);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .region(AWS_REGION)
                .logicalResourceIdentifier("logicalId")
                .clientRequestToken("token")
                .build();

        when(proxyClient.client().createCluster(any(CreateClusterRequest.class))).thenReturn(createClusterResponseSdk());

        when(proxyClient.client().enableLogging(any(EnableLoggingRequest.class)))
                .thenReturn(EnableLoggingResponse.builder()
                        .bucketName(BUCKET_NAME)
                        .loggingEnabled(true)
                        .lastSuccessfulDeliveryTime(Instant.now())
                        .build());

        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class))).thenReturn(describeClustersResponseSdk());

        when(proxyClient.client().describeLoggingStatus(any(DescribeLoggingStatusRequest.class)))
                .thenReturn(DescribeLoggingStatusResponse.builder()
                        .bucketName(BUCKET_NAME)
                        .loggingEnabled(true)
                        .lastSuccessfulDeliveryTime(Instant.now())
                        .build());
        when(proxyClient.client().getResourcePolicy(any(GetResourcePolicyRequest.class))).thenReturn(getEmptyResourcePolicyResponseSdk());

        ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(30);

        response = handler.handleRequest(proxy, request, response.getCallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getResourceModel().getClusterNamespaceArn()).
                isEqualTo(request.getDesiredResourceState().getClusterNamespaceArn());
        assertThat(response.getResourceModel().getClusterIdentifier()).
                isEqualTo(request.getDesiredResourceState().getClusterIdentifier());
        verify(proxyClient.client()).createCluster(any(CreateClusterRequest.class));
        verify(proxyClient.client(), times(4))
                .describeClusters(any(DescribeClustersRequest.class));

    }

    @Test
    public void testCreateCluster_MultiAZ() {
        ResourceModel requestModel = createClusterRequestModel();
        requestModel.setMultiAZ(true);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .region(AWS_REGION)
                .logicalResourceIdentifier("logicalId")
                .clientRequestToken("token")
                .build();

        when(proxyClient.client().createCluster(any(CreateClusterRequest.class)))
                .thenReturn(CreateClusterResponse.builder()
                        .cluster(MULTIAZ_CLUSTER)
                        .build());

        Cluster multiAZCluster = Cluster.builder()
                .clusterIdentifier(CLUSTER_IDENTIFIER)
                .masterUsername(MASTER_USERNAME)
                .nodeType(NODETYPE)
                .numberOfNodes(NUMBER_OF_NODES)
                .clusterStatus("available")
                .clusterAvailabilityStatus("Available")
                .allowVersionUpgrade(true)
                .automatedSnapshotRetentionPeriod(0)
                .encrypted(true)
                .multiAZ(MULTIAZ_ENABLED)
                .enhancedVpcRouting(false)
                .manualSnapshotRetentionPeriod(1)
                .publiclyAccessible(false)
                .clusterSecurityGroups(Collections.emptyList())
                .iamRoles(Collections.emptyList())
                .vpcSecurityGroups(Collections.emptyList())
                .build();

        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class)))
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(multiAZCluster)
                        .build());

        when(proxyClient.client().describeLoggingStatus(any(DescribeLoggingStatusRequest.class)))
                .thenReturn(DescribeLoggingStatusResponse.builder().loggingEnabled(false).build());
        when(proxyClient.client().getResourcePolicy(any(GetResourcePolicyRequest.class))).thenReturn(getEmptyResourcePolicyResponseSdk());

        ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        response.getResourceModel().setMasterUserPassword(MASTER_USERPASSWORD);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(30);

        response = handler.handleRequest(proxy, request, response.getCallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getResourceModel().getClusterIdentifier()).
                isEqualTo(request.getDesiredResourceState().getClusterIdentifier());
        assertThat(response.getResourceModel().getMultiAZ()).
                isEqualTo(request.getDesiredResourceState().getMultiAZ());

        verify(proxyClient.client()).createCluster(any(CreateClusterRequest.class));
        verify(proxyClient.client(), times(3))
                .describeClusters(any(DescribeClustersRequest.class));
    }

    @Test
    public void testPutNamespaceResourcePolicy() {
        ResourceModel requestModel = createClusterRequestModel();
        requestModel.setNamespaceResourcePolicy(Translator.convertStringToJson(NAMESPACE_POLICY, logger));

        ResourceModel responseModel = createClusterResponseModel();
        responseModel.setLoggingProperties(LOGGING_PROPERTIES_DISABLED);
        responseModel.setNamespaceResourcePolicy(Translator.convertStringToJson(NAMESPACE_POLICY, logger));

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .region(AWS_REGION)
                .logicalResourceIdentifier("logicalId")
                .clientRequestToken("token")
                .build();

        when(proxyClient.client().createCluster(any(CreateClusterRequest.class))).thenReturn(createClusterResponseSdk());
        when(proxyClient.client().putResourcePolicy(any(PutResourcePolicyRequest.class))).thenReturn(putResourcePolicyResponseSdk());
        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class))).thenReturn(describeClustersResponseSdk());
        when(proxyClient.client().getResourcePolicy(any(GetResourcePolicyRequest.class))).thenReturn(getResourcePolicyResponseSdk());
        when(proxyClient.client().describeLoggingStatus(any(DescribeLoggingStatusRequest.class)))
                .thenReturn(DescribeLoggingStatusResponse.builder().loggingEnabled(false).build());

        ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(30);

        response = handler.handleRequest(proxy, request, response.getCallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getResourceModel()).isEqualTo(responseModel);
        verify(proxyClient.client()).createCluster(any(CreateClusterRequest.class));
        verify(proxyClient.client(), times(4))
                .describeClusters(any(DescribeClustersRequest.class));
    }

    @Test
    public void testCreateClusterModifyClusterMaintenance() {
        ResourceModel requestModel = createClusterRequestModel();
        requestModel.setDeferMaintenance(true);
        requestModel.setDeferMaintenanceDuration(DEFER_MAINTENANCE_DURATION);
        requestModel.setDeferMaintenanceStartTime(DEFER_MAINTENANCE_START_TIME);

        ResourceModel responseModel = createClusterResponseModel();
        responseModel.setLoggingProperties(LOGGING_PROPERTIES_DISABLED);
        responseModel.setDeferMaintenanceIdentifier(DEFER_MAINTENANCE_IDENTIFIER);
        responseModel.setDeferMaintenanceEndTime(DEFER_MAINTENANCE_END_TIME);
        responseModel.setDeferMaintenanceStartTime(DEFER_MAINTENANCE_START_TIME);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .region(AWS_REGION)
                .logicalResourceIdentifier("logicalId")
                .clientRequestToken("token")
                .build();

        when(proxyClient.client().createCluster(any(CreateClusterRequest.class))).thenReturn(createClusterResponseSdk());
        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class))).thenReturn(describeClustersResponseWithDeferMaintenanceSdk());
        when(proxyClient.client().getResourcePolicy(any(GetResourcePolicyRequest.class))).thenReturn(getEmptyResourcePolicyResponseSdk());
        when(proxyClient.client().modifyClusterMaintenance(any(ModifyClusterMaintenanceRequest.class))).thenReturn(getModifyClusterMaintenanceResponseSdk());
        when(proxyClient.client().describeLoggingStatus(any(DescribeLoggingStatusRequest.class)))
                .thenReturn(DescribeLoggingStatusResponse.builder().loggingEnabled(false).build());

        ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(30);

        response = handler.handleRequest(proxy, request, response.getCallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getResourceModel()).isEqualTo(responseModel);
        verify(proxyClient.client()).createCluster(any(CreateClusterRequest.class));
        verify(proxyClient.client(), times(4))
                .describeClusters(any(DescribeClustersRequest.class));
    }

    @Test
    public void testCreateCluster_ManagedAdminPassword() {
        ResourceModel requestModel = createClusterRequestModel();
        requestModel.setMasterUserPassword(null);
        requestModel.setManageMasterPassword(true);
        requestModel.setMasterPasswordSecretKmsKeyId(MASTER_PASSWORD_SECRET_KMS_KEY_ID);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .region(AWS_REGION)
                .logicalResourceIdentifier("logicalId")
                .clientRequestToken("token")
                .build();

        when(proxyClient.client().createCluster(any(CreateClusterRequest.class)))
                .thenReturn(CreateClusterResponse.builder()
                        .cluster(MANAGED_ADMIN_PASSWORD_CLUSTER)
                        .build());
        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class)))
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(MANAGED_ADMIN_PASSWORD_CLUSTER)
                        .build());
        when(proxyClient.client().describeLoggingStatus(any(DescribeLoggingStatusRequest.class)))
                .thenReturn(DescribeLoggingStatusResponse.builder().loggingEnabled(false).build());
        when(proxyClient.client().getResourcePolicy(any(GetResourcePolicyRequest.class))).thenReturn(getEmptyResourcePolicyResponseSdk());

        ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(30);

        response = handler.handleRequest(proxy, request, response.getCallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getResourceModel().getClusterIdentifier()).
                isEqualTo(request.getDesiredResourceState().getClusterIdentifier());
        assertThat(response.getResourceModel().getMasterPasswordSecretArn()).
                isEqualTo(MASTER_PASSWORD_SECRET_ARN);
        assertThat(response.getResourceModel().getMasterPasswordSecretKmsKeyId()).
                isEqualTo(request.getDesiredResourceState().getMasterPasswordSecretKmsKeyId());
        assertThat(response.getResourceModel().getMasterUserPassword()).isNull();

        verify(proxyClient.client()).createCluster(any(CreateClusterRequest.class));
        verify(proxyClient.client(), times(3))
                .describeClusters(any(DescribeClustersRequest.class));
    }

    @Test
    public void testRestoreCluster_ManagedAdminPassword() {
        ResourceModel requestModel = restoreClusterRequestModel();
        requestModel.setMasterUserPassword(null);
        requestModel.setManageMasterPassword(true);
        requestModel.setMasterPasswordSecretKmsKeyId(MASTER_PASSWORD_SECRET_KMS_KEY_ID);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestModel)
                .region(AWS_REGION)
                .logicalResourceIdentifier("logicalId")
                .clientRequestToken("token")
                .build();

        when(proxyClient.client().restoreFromClusterSnapshot(any(RestoreFromClusterSnapshotRequest.class))).thenReturn(RestoreFromClusterSnapshotResponse.builder()
                .cluster(MANAGED_ADMIN_PASSWORD_CLUSTER)
                .build());
        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class)))
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(MANAGED_ADMIN_PASSWORD_CLUSTER)
                        .build());
        when(proxyClient.client().describeLoggingStatus(any(DescribeLoggingStatusRequest.class)))
                .thenReturn(DescribeLoggingStatusResponse.builder().loggingEnabled(false).build());
        when(proxyClient.client().getResourcePolicy(any(GetResourcePolicyRequest.class))).thenReturn(getEmptyResourcePolicyResponseSdk());

        ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(30);

        response = handler.handleRequest(proxy, request, response.getCallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getResourceModel().getClusterIdentifier()).
                isEqualTo(request.getDesiredResourceState().getClusterIdentifier());
        assertThat(response.getResourceModel().getMasterPasswordSecretArn()).
                isEqualTo(MASTER_PASSWORD_SECRET_ARN);
        assertThat(response.getResourceModel().getMasterPasswordSecretKmsKeyId()).
                isEqualTo(request.getDesiredResourceState().getMasterPasswordSecretKmsKeyId());
        assertThat(response.getResourceModel().getMasterUserPassword()).isNull();

        verify(proxyClient.client()).restoreFromClusterSnapshot(any(RestoreFromClusterSnapshotRequest.class));
        verify(proxyClient.client(), times(3))
                .describeClusters(any(DescribeClustersRequest.class));
    }

}
