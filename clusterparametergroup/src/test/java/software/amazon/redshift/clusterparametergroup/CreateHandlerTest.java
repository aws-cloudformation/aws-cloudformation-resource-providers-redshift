package software.amazon.redshift.clusterparametergroup;

import java.time.Duration;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.*;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static software.amazon.redshift.clusterparametergroup.TestUtils.*;

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
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(RedshiftClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
        handler = new CreateHandler();
    }

    @Test
    public void handleRequest_SimpleInProgress() {

        final ResourceModel model = COMPLETE_MODEL;

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .region(AWS_REGION)
                .logicalResourceIdentifier("logicalId")
                .clientRequestToken("token")
                .desiredResourceTags(DESIRED_RESOURCE_TAGS)
                .build();

        when(proxyClient.client().createClusterParameterGroup(any(CreateClusterParameterGroupRequest.class)))
                .thenReturn(CreateClusterParameterGroupResponse.builder()
                        .clusterParameterGroup(CLUSTER_PARAMETER_GROUP)
                        .build());

//        when(proxyClient.client().describeClusterParameterGroups(any(DescribeClusterParameterGroupsRequest.class)))
//                .thenReturn(DescribeClusterParameterGroupsResponse.builder()
//                        .parameterGroups(CLUSTER_PARAMETER_GROUP)
//                        .marker("0")
//                        .build());

        when(proxyClient.client().modifyClusterParameterGroup(any(ModifyClusterParameterGroupRequest.class)))
                .thenReturn(ModifyClusterParameterGroupResponse.builder()
                        .parameterGroupName(PARAMETER_GROUP_NAME)
                        .parameterGroupStatus("Your parameter group has been updated")
                        .build());

        when(proxyClient.client().describeClusterParameters(any(DescribeClusterParametersRequest.class)))
                .thenReturn(DescribeClusterParametersResponse.builder()
                        .parameters(SDK_PARAMETERS)
                        .marker("")
                        .build());

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.IN_PROGRESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(300);
//        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        verify(proxyClient.client()).createClusterParameterGroup(any(CreateClusterParameterGroupRequest.class));
        verify(proxyClient.client()).describeClusterParameters(any(DescribeClusterParametersRequest.class));

    }

    @Test
    public void handleRequest_SimpleSuccess() {

        final ResourceModel model = COMPLETE_MODEL;

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .region(AWS_REGION)
                .logicalResourceIdentifier("logicalId")
                .clientRequestToken("token")
                .desiredResourceTags(DESIRED_RESOURCE_TAGS)
                .build();

        when(proxyClient.client().createClusterParameterGroup(any(CreateClusterParameterGroupRequest.class)))
                .thenReturn(CreateClusterParameterGroupResponse.builder()
                        .clusterParameterGroup(CLUSTER_PARAMETER_GROUP)
                        .build());

        when(proxyClient.client().describeClusterParameterGroups(any(DescribeClusterParameterGroupsRequest.class)))
                .thenReturn(DescribeClusterParameterGroupsResponse.builder()
                        .parameterGroups(CLUSTER_PARAMETER_GROUP)
                        .marker("")
                        .build());

        CallbackContext callbackContext = new CallbackContext();
        callbackContext.setParametersApplied(true);

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, callbackContext, proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        verify(proxyClient.client()).createClusterParameterGroup(any(CreateClusterParameterGroupRequest.class));
        verify(proxyClient.client()).describeClusterParameterGroups(any(DescribeClusterParameterGroupsRequest.class));

    }


}
