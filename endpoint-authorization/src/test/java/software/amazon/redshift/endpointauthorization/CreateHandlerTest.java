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
import software.amazon.awssdk.services.redshift.model.AuthorizeEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.AuthorizeEndpointAccessResponse;
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
public class CreateHandlerTest extends AbstractTestBase {
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
    @DisplayName("CreateEndpointAuthorizationSdkCallTest")
    class CreateEndpointAuthorizationSdkCallTest {
        AuthorizeEndpointAccessRequest request;
        CreateHandler handler;

        @BeforeEach
        public void setup() {
            request = AuthorizeEndpointAccessRequest.builder().build();
            handler = new CreateHandler();
        }

        @Test
        public void testHappyResponse() {
            AuthorizeEndpointAccessResponse response = AuthorizeEndpointAccessResponse.builder().build();

            when(proxyClient.client().authorizeEndpointAccess(any(AuthorizeEndpointAccessRequest.class)))
                    .thenReturn(response);
            assertEquals(response, handler.createEndpointAuthorization(request, proxyClient));
        }

        @Test
        public void testErrorHandling() {
            when(proxyClient.client().authorizeEndpointAccess(any(AuthorizeEndpointAccessRequest.class)))
                    .thenThrow(RedshiftException.class);
            assertThrows(
                    CfnGeneralServiceException.class,
                    () -> handler.createEndpointAuthorization(request, proxyClient)
            );
        }
    }

    @Nested
    @DisplayName("CreateEndpointAuthorization:Handler")
    class CreateEndpointAuthorizationTest {
        @AfterEach
        public void tear_down() {
            verify(sdkClient, atLeastOnce()).serviceName();
            verifyNoMoreInteractions(sdkClient);
        }

        @Test
        public void testHandleRequest() {
            final CreateHandler handler = new CreateHandler();

            final ResourceModel model = ResourceModel.builder().build();

            final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                    .desiredResourceState(model)
                    .build();

            try (MockedStatic<Translator> mockedTranslator = mockStatic(Translator.class)) {
                AuthorizeEndpointAccessRequest createRequest = AuthorizeEndpointAccessRequest.builder().build();
                DescribeEndpointAuthorizationRequest describeRequest = DescribeEndpointAuthorizationRequest.builder().build();
                mockedTranslator.when(() -> Translator.translateToCreateRequest(model)).thenReturn(createRequest);
                mockedTranslator.when(() -> Translator.translateToReadRequest(model)).thenReturn(describeRequest);
                mockedTranslator.when(() -> Translator.translateFromReadResponse(any(DescribeEndpointAuthorizationResponse.class)))
                        .thenReturn(model);

                try (MockedStatic<Stabilizers> mockedStabilizers = mockStatic(Stabilizers.class)) {
                    mockedStabilizers.when(() -> Stabilizers.isAuthorized(any(), any(), any())).thenReturn(true);

                    when(proxyClient.client().authorizeEndpointAccess(any(AuthorizeEndpointAccessRequest.class)))
                            .thenReturn(AuthorizeEndpointAccessResponse.builder().build());
                    when(proxyClient.client().describeEndpointAuthorization(any(DescribeEndpointAuthorizationRequest.class)))
                            .thenReturn(DescribeEndpointAuthorizationResponse.builder().build());

                    final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(
                            proxy, request, new CallbackContext(), proxyClient, logger);

                    assertThat(response).isNotNull();
                    assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
                    assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
                    assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
                    assertThat(response.getResourceModels()).isNull();
                    assertThat(response.getMessage()).isNull();
                    assertThat(response.getErrorCode()).isNull();
                }
            }
        }
    }
}
