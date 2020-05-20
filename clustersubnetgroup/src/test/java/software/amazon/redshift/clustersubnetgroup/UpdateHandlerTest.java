package software.amazon.redshift.clustersubnetgroup;

import org.junit.jupiter.api.AfterEach;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.awscore.AwsRequest;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetGroup;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetQuotaExceededException;
import software.amazon.awssdk.services.redshift.model.CreateTagsRequest;
import software.amazon.awssdk.services.redshift.model.DeleteTagsRequest;
import software.amazon.awssdk.services.redshift.model.DependentServiceRequestThrottlingException;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSubnetGroupsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSubnetGroupsResponse;
import software.amazon.awssdk.services.redshift.model.DescribeTagsRequest;
import software.amazon.awssdk.services.redshift.model.InvalidSubnetException;
import software.amazon.awssdk.services.redshift.model.ModifyClusterSubnetGroupRequest;
import software.amazon.awssdk.services.redshift.model.ModifyClusterSubnetGroupResponse;
import software.amazon.awssdk.services.redshift.model.Subnet;
import software.amazon.awssdk.services.redshift.model.SubnetAlreadyInUseException;
import software.amazon.awssdk.services.redshift.model.Tag;
import software.amazon.awssdk.services.redshift.model.UnauthorizedOperationException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
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
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static junit.framework.Assert.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static software.amazon.redshift.clustersubnetgroup.TestUtils.AWS_ACCOUNT_ID;
import static software.amazon.redshift.clustersubnetgroup.TestUtils.AWS_REGION;
import static software.amazon.redshift.clustersubnetgroup.TestUtils.BASIC_CLUSTER_SUBNET_GROUP;
import static software.amazon.redshift.clustersubnetgroup.TestUtils.BASIC_MODEL;
import static software.amazon.redshift.clustersubnetgroup.TestUtils.CREATE_TAGS_REQUEST;
import static software.amazon.redshift.clustersubnetgroup.TestUtils.DELETE_TAGS_REQUEST;
import static software.amazon.redshift.clustersubnetgroup.TestUtils.DESCRIBE_TAGS_REQUEST;
import static software.amazon.redshift.clustersubnetgroup.TestUtils.DESCRIBE_TAGS_RESPONSE;
import static software.amazon.redshift.clustersubnetgroup.TestUtils.DESCRIBE_TAGS_RESPONSE_CREATING;
import static software.amazon.redshift.clustersubnetgroup.TestUtils.DESIRED_RESOURCE_TAGS;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<RedshiftClient> proxyClient;

    @Mock
    RedshiftClient sdkClient;

    private UpdateHandler handler;

    @BeforeEach
    public void setup() {
        handler = new UpdateHandler();
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
                .desiredResourceTags(DESIRED_RESOURCE_TAGS)
                .logicalResourceIdentifier("logicalId")
                .clientRequestToken("token")
                .build();

        when(proxyClient.client().modifyClusterSubnetGroup(any(ModifyClusterSubnetGroupRequest.class)))
                .thenReturn(ModifyClusterSubnetGroupResponse.builder()
                        .clusterSubnetGroup(BASIC_CLUSTER_SUBNET_GROUP)
                        .build());

        when(proxyClient.client().describeTags(any(DescribeTagsRequest.class)))
                .thenReturn(DESCRIBE_TAGS_RESPONSE);

        when(proxyClient.client().describeClusterSubnetGroups(any(DescribeClusterSubnetGroupsRequest.class)))
                .thenReturn(DescribeClusterSubnetGroupsResponse.builder()
                        .clusterSubnetGroups(BASIC_CLUSTER_SUBNET_GROUP)
                        .build());

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_UpdateTags() {
        final ResourceModel model = BASIC_MODEL;

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .region(AWS_REGION)
                .awsAccountId(AWS_ACCOUNT_ID)
                .desiredResourceTags(DESIRED_RESOURCE_TAGS)
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

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        ArgumentCaptor<CreateTagsRequest> createTagArgument = ArgumentCaptor.forClass(CreateTagsRequest.class);
        verify(proxyClient.client()).createTags(createTagArgument.capture());
        assertEquals(CREATE_TAGS_REQUEST.tags(), createTagArgument.getValue().tags());

        ArgumentCaptor<DeleteTagsRequest> deleteTagArgument = ArgumentCaptor.forClass(DeleteTagsRequest.class);
        verify(proxyClient.client()).deleteTags(deleteTagArgument.capture());
        assertEquals(DELETE_TAGS_REQUEST.tagKeys(), deleteTagArgument.getValue().tagKeys());
    }
}
