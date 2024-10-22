package software.amazon.redshift.integration;

import java.time.Duration;

import lombok.Getter;
import org.mockito.Mockito;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.DescribeIntegrationsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeIntegrationsResponse;
import software.amazon.awssdk.services.redshift.model.IntegrationNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReadHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<RedshiftClient> proxyClient;

    @Mock
    RedshiftClient RedshiftClient;

    @Getter
    private ReadHandler handler;

    @BeforeEach
    public void setup() {
        handler = new ReadHandler();
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        RedshiftClient = mock(RedshiftClient.class);
        proxyClient = MOCK_PROXY(proxy, RedshiftClient);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        when(proxyClient.client().describeIntegrations(any(DescribeIntegrationsRequest.class)))
                .thenReturn(DescribeIntegrationsResponse.builder()
                        .integrations(INTEGRATION_ACTIVE)
                        .build());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(INTEGRATION_ACTIVE_MODEL)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();

        verify(proxyClient.client(), times(1))
                .describeIntegrations(
                        Mockito.<DescribeIntegrationsRequest>argThat(
                                req -> INTEGRATION_ARN.equals(req.integrationArn()))
                );
    }

    @Test
    public void handleRequest_readApiFailure() {
        final String notFoundMessage = "Integration not found error";
        when(proxyClient.client().describeIntegrations(any(DescribeIntegrationsRequest.class)))
                .thenThrow(IntegrationNotFoundException.builder()
                        .message(notFoundMessage)
                        .build());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(INTEGRATION_ACTIVE_MODEL)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.FAILED);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isEqualTo(notFoundMessage);
        assertThat(response.getErrorCode()).isEqualTo(HandlerErrorCode.NotFound);

        verify(proxyClient.client(), times(1))
                .describeIntegrations(
                        Mockito.<DescribeIntegrationsRequest>argThat(
                                req -> INTEGRATION_ARN.equals(req.integrationArn()))
                );
    }

    @Test
    public void handleRequest_missingIntegrationArn() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(INTEGRATION_ACTIVE_MODEL.toBuilder().integrationArn(null).build())
            .build();

        assertThrows(RuntimeException.class, () -> {
            handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        });
    }
}
