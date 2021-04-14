package software.amazon.redshift.endpointauthorization;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.RedshiftException;
import software.amazon.awssdk.services.redshift.model.RevokeEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.RevokeEndpointAccessResponse;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;

import static junit.framework.Assert.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DeleteHandlerTest extends AbstractTestBase {
    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<RedshiftClient> proxyClient;

    @Mock
    RedshiftClient sdkClient;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(RedshiftClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
    }

    @Nested
    @DisplayName("RevokeEndpointAccess")
    class RevokeEndpointAccessTest {
        RevokeEndpointAccessRequest request;
        DeleteHandler handler;

        @BeforeEach
        public void setup() {
            request = RevokeEndpointAccessRequest.builder().build();
            handler = new DeleteHandler();
        }

        @Test
        public void testHappyResponse() {
            RevokeEndpointAccessResponse response = RevokeEndpointAccessResponse.builder().build();

            when(proxyClient.client().revokeEndpointAccess(any(RevokeEndpointAccessRequest.class)))
                    .thenReturn(response);
            assertEquals(response, handler.revokeEndpointAccess(request, proxyClient));
        }

        @Test
        public void testErrorHandling() {
            when(proxyClient.client().revokeEndpointAccess(any(RevokeEndpointAccessRequest.class)))
                    .thenThrow(RedshiftException.class);
            assertThrows(
                    CfnGeneralServiceException.class, () -> handler.revokeEndpointAccess(request, proxyClient)
            );
        }
    }

    @Nested
    @DisplayName("RevokeHandlerTests")
    class RevokeHandlerTest {
        @AfterEach
        public void tear_down() {
            verify(sdkClient, atLeastOnce()).serviceName();
            verifyNoMoreInteractions(sdkClient);
        }

        @Test
        public void testHandleRequest() {
            final DeleteHandler handler = new DeleteHandler();

            final ResourceModel model = ResourceModel.builder().build();

            final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                    .desiredResourceState(model)
                    .build();

            try (MockedStatic<Translator> mockedTranslator = mockStatic(Translator.class)) {
                RevokeEndpointAccessRequest deleteRequest = RevokeEndpointAccessRequest.builder().build();
                mockedTranslator.when(() -> Translator.translateToRevokeRequest(model)).thenReturn(deleteRequest);

                try (MockedStatic<Stabilizers> mockedStabilizers = mockStatic(Stabilizers.class)) {
                    mockedStabilizers.when(() -> Stabilizers.isDoneRevoking(any(), any(), any())).thenReturn(true);

                    when(proxyClient.client().revokeEndpointAccess(any(RevokeEndpointAccessRequest.class)))
                            .thenReturn(RevokeEndpointAccessResponse.builder().build());

                    final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(
                            proxy, request, new CallbackContext(), proxyClient, logger
                    );

                    assertThat(response).isNotNull();
                    assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
                    assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
                    assertThat(response.getResourceModel()).isNull();
                    assertThat(response.getResourceModels()).isNull();
                    assertThat(response.getMessage()).isNull();
                    assertThat(response.getErrorCode()).isNull();
                }
            }
        }
    }
}
