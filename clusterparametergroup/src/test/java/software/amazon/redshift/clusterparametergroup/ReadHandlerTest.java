package software.amazon.redshift.clusterparametergroup;

import java.time.Duration;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.DescribeClusterParameterGroupsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterParameterGroupsResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static software.amazon.redshift.clusterparametergroup.TestUtils.AWS_REGION;
import static software.amazon.redshift.clusterparametergroup.TestUtils.CLUSTER_PARAMETER_GROUP;
import static software.amazon.redshift.clusterparametergroup.TestUtils.COMPLETE_MODEL;
import static software.amazon.redshift.clusterparametergroup.TestUtils.DESIRED_RESOURCE_TAGS;

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
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(RedshiftClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
        handler = new ReadHandler();
    }

    @Test
    public void handleRequest_ResourceNotFound() {
        final ResourceModel model = COMPLETE_MODEL;
        // set group name to null, which is not part of DescribeParameterGroups response
        model.setParameterGroupName(null);

        when(proxyClient.client().describeClusterParameterGroups(any(DescribeClusterParameterGroupsRequest.class)))
                .thenReturn(DescribeClusterParameterGroupsResponse.builder()
                        .parameterGroups(CLUSTER_PARAMETER_GROUP)
                        .marker("")
                        .build());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
    }

    @Test
    public void handleRequest_Success() {
        final ResourceModel model = COMPLETE_MODEL;
        when(proxyClient.client().describeClusterParameterGroups(any(DescribeClusterParameterGroupsRequest.class)))
                .thenReturn(DescribeClusterParameterGroupsResponse.builder()
                        .parameterGroups(CLUSTER_PARAMETER_GROUP)
                        .build());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .region(AWS_REGION)
                .logicalResourceIdentifier("logicalId")
                .clientRequestToken("token")
                .desiredResourceTags(DESIRED_RESOURCE_TAGS)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        System.out.println("Read response is:: " + response);
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
    }
}
