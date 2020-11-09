package software.amazon.redshift.cluster;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.CreateClusterRequest;
import software.amazon.awssdk.services.redshift.model.CreateClusterResponse;
import software.amazon.awssdk.services.redshift.model.CreateClusterSubnetGroupRequest;
import software.amazon.awssdk.services.redshift.model.CreateClusterSubnetGroupResponse;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSubnetGroupsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSubnetGroupsResponse;
import software.amazon.awssdk.services.redshift.model.DescribeClustersRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClustersResponse;
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
import static software.amazon.redshift.cluster.TestUtils.AWS_REGION;
import static software.amazon.redshift.cluster.TestUtils.BASIC_CLUSTER;
import static software.amazon.redshift.cluster.TestUtils.BASIC_CLUSTER_READ;
import static software.amazon.redshift.cluster.TestUtils.BASIC_MODEL;

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
        ResourceModel model = BASIC_MODEL;

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .region(AWS_REGION)
                .logicalResourceIdentifier("logicalId")
                .clientRequestToken("token")
                .build();

        when(proxyClient.client().createCluster(any(CreateClusterRequest.class)))
                .thenReturn(CreateClusterResponse.builder()
                        .cluster(BASIC_CLUSTER)
                        .build());

        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class)))
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(BASIC_CLUSTER_READ)
                        .build());

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());

        Assert.assertEquals("expected not equal to actual",response.getResourceModel(), request.getDesiredResourceState());

        verify(proxyClient.client()).createCluster(any(CreateClusterRequest.class));
        verify(proxyClient.client(), times(3))
                .describeClusters(any(DescribeClustersRequest.class));

    }
}
