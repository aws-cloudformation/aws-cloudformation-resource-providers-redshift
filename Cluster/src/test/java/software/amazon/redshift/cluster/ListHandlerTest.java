package software.amazon.redshift.cluster;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSubnetGroupsResponse;
import software.amazon.awssdk.services.redshift.model.DescribeClustersRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClustersResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static software.amazon.redshift.cluster.AbstractTestBase.MOCK_PROXY;
import static software.amazon.redshift.cluster.TestUtils.AWS_REGION;
import static software.amazon.redshift.cluster.TestUtils.BASIC_CLUSTER;
import static software.amazon.redshift.cluster.TestUtils.BASIC_MODEL;
import static software.amazon.redshift.cluster.TestUtils.CLUSTER_IDENTIFIER;

@ExtendWith(MockitoExtension.class)
public class ListHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<RedshiftClient> proxyClient;

    @Mock
    RedshiftClient sdkClient;

    @Mock
    private Logger logger;

    private ListHandler handler ;

    @BeforeEach
    public void setup() {
        handler = new ListHandler();
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ResourceModel model = BASIC_MODEL;

        final DescribeClustersResponse describeClustersResponse = DescribeClustersResponse.builder()
                .clusters(BASIC_CLUSTER)
                .build();

        doReturn(describeClustersResponse).when(proxy).injectCredentialsAndInvokeV2(any(), any());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .region(AWS_REGION)
                .build();


        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isNull();
        assertThat(response.getResourceModels()).isNotNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }
}
