package software.amazon.redshift.clusterparametergroup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.DescribeClusterParameterGroupsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterParameterGroupsResponse;
import software.amazon.awssdk.services.redshift.model.DescribeClusterParametersRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterParametersResponse;
import software.amazon.awssdk.services.redshift.model.DescribeTagsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeTagsResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static software.amazon.redshift.clusterparametergroup.TestUtils.AWS_REGION;
import static software.amazon.redshift.clusterparametergroup.TestUtils.CLUSTER_PARAMETER_GROUP;
import static software.amazon.redshift.clusterparametergroup.TestUtils.COMPLETE_MODEL;
import static software.amazon.redshift.clusterparametergroup.TestUtils.DESIRED_RESOURCE_TAGS;
import static software.amazon.redshift.clusterparametergroup.TestUtils.DESCRIBE_TAGS_RESPONSE_CREATING;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends AbstractTestBase {

    @Mock
    RedshiftClient sdkClient;
    @Mock
    private AmazonWebServicesClientProxy proxy;
    @Mock
    private ProxyClient<RedshiftClient> proxyClient;
    private ReadHandler handler;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(RedshiftClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
        handler = new ReadHandler();
    }

    @Test
    public void handleRequest_Success() {
        final ResourceModel model = COMPLETE_MODEL;

        when(proxyClient.client().describeClusterParameters(any(DescribeClusterParametersRequest.class)))
                .thenReturn(DescribeClusterParametersResponse.builder().build());

        when(proxyClient.client().describeClusterParameterGroups(any(DescribeClusterParameterGroupsRequest.class)))
                .thenReturn(DescribeClusterParameterGroupsResponse.builder()
                        .parameterGroups(CLUSTER_PARAMETER_GROUP)
                        .build());

        when(proxyClient.client().describeTags(any(DescribeTagsRequest.class)))
                .thenReturn(DESCRIBE_TAGS_RESPONSE_CREATING);

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .region(AWS_REGION)
                .logicalResourceIdentifier("logicalId")
                .clientRequestToken("token")
                .desiredResourceTags(DESIRED_RESOURCE_TAGS)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
    }
}
