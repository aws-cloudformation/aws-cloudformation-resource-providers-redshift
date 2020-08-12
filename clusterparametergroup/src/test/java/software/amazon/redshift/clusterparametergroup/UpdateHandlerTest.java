package software.amazon.redshift.clusterparametergroup;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.*;
import software.amazon.awssdk.services.redshift.model.Tag;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static software.amazon.redshift.clusterparametergroup.TestUtils.*;

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
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(RedshiftClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
        handler = new UpdateHandler();
    }

    @Test
    public void handleRequest_SimpleSuccess() {

        final ResourceModel model = COMPLETE_MODEL;

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .desiredResourceTags(DESIRED_RESOURCE_TAGS)
                .region(AWS_REGION)
                .build();

        when(proxyClient.client().modifyClusterParameterGroup(any(ModifyClusterParameterGroupRequest.class)))
                .thenReturn(ModifyClusterParameterGroupResponse.builder()
                        .parameterGroupName(PARAMETER_GROUP_NAME)
                        .parameterGroupStatus("Your parameter group has been updated")
                        .build());

        when(proxyClient.client().describeTags(any(DescribeTagsRequest.class)))
                .thenReturn(DESCRIBE_TAGS_RESPONSE);

        when(proxyClient.client().describeClusterParameterGroups(any(DescribeClusterParameterGroupsRequest.class)))
                .thenReturn(DescribeClusterParameterGroupsResponse.builder()
                        .parameterGroups(CLUSTER_PARAMETER_GROUP)
                        .build());

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
//        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_UpdateTags() {

        final ResourceModel model = COMPLETE_MODEL;

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .desiredResourceTags(DESIRED_RESOURCE_TAGS)
                .region(AWS_REGION)
                .build();

        when(proxyClient.client().modifyClusterParameterGroup(any(ModifyClusterParameterGroupRequest.class)))
                .thenReturn(ModifyClusterParameterGroupResponse.builder()
                        .parameterGroupName(PARAMETER_GROUP_NAME)
                        .parameterGroupStatus("Your parameter group has been updated")
                        .build());

        when(proxyClient.client().describeTags(any(DescribeTagsRequest.class)))
                .thenReturn(DESCRIBE_TAGS_RESPONSE_CREATING);

        when(proxyClient.client().describeClusterParameterGroups(any(DescribeClusterParameterGroupsRequest.class)))
                .thenReturn(DescribeClusterParameterGroupsResponse.builder()
                        .parameterGroups(CLUSTER_PARAMETER_GROUP)
                        .build());

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
//        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        ArgumentCaptor<CreateTagsRequest> createTagArgument = ArgumentCaptor.forClass(CreateTagsRequest.class);
        verify(proxyClient.client()).createTags(createTagArgument.capture());
//        assertEquals(CREATE_TAGS_REQUEST.tags(), createTagArgument.getValue().tags());

        ArgumentCaptor<DeleteTagsRequest> deleteTagArgument = ArgumentCaptor.forClass(DeleteTagsRequest.class);
        verify(proxyClient.client()).deleteTags(deleteTagArgument.capture());
//        assertEquals(DELETE_TAGS_REQUEST.tagKeys(), deleteTagArgument.getValue().tagKeys());
    }
}
