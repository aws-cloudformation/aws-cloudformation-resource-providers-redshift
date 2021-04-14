package software.amazon.redshift.endpointauthorization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAuthorizationRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAuthorizationResponse;
import software.amazon.awssdk.services.redshift.model.RedshiftException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<RedshiftClient> proxyClient;

    @Mock
    RedshiftClient sdkClient;

    ReadHandler handler;


    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(RedshiftClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
        handler = new ReadHandler();
    }

    @Nested
    @DisplayName("TestReadEndpointAuthorization")
    class TestReadEndpointAuthorization {
        DescribeEndpointAuthorizationRequest request;

        @BeforeEach
        public void setup() {
            request = DescribeEndpointAuthorizationRequest.builder().build();
            handler = new ReadHandler();
        }

        @Test
        public void testHappyResponse() {
            DescribeEndpointAuthorizationResponse response = DescribeEndpointAuthorizationResponse.builder().build();

            when(proxyClient.client().describeEndpointAuthorization(any(DescribeEndpointAuthorizationRequest.class)))
                    .thenReturn(response);
            assertEquals(response, handler.readEndpointAuthorization(request, proxyClient));
        }

        @Test
        public void testErrorHandling() {
            when(proxyClient.client().describeEndpointAuthorization(any(DescribeEndpointAuthorizationRequest.class)))
                    .thenThrow(RedshiftException.class);
            assertThrows(
                    CfnGeneralServiceException.class,
                    () -> handler.readEndpointAuthorization(request, proxyClient)
            );
        }
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ResourceModel model = ResourceModel.builder().build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(
                proxy, request, new CallbackContext(), proxyClient, logger
        );

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }
}
