package software.amazon.redshift.endpointauthorization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.AuthorizeEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.AuthorizeEndpointAccessResponse;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAuthorizationRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAuthorizationResponse;
import software.amazon.awssdk.services.redshift.model.RevokeEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.RevokeEndpointAccessResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends AbstractTestBase {
    @Mock
    private RedshiftClient sdkClient;

    private AmazonWebServicesClientProxy proxy;
    private ProxyClient<RedshiftClient> proxyClient;


    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxyClient = MOCK_PROXY(proxy, sdkClient);
    }

    @Test
    public void testHandleRequest() {
        final UpdateHandler handler = new UpdateHandler();

        final ResourceModel model = ResourceModel.builder().build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .previousResourceState(model)
                .build();

        try (MockedStatic<Translator> mockedTranslator = mockStatic(Translator.class)) {
            try (MockedStatic<Validator> mockedValidator = Mockito.mockStatic(Validator.class)) {
                AuthorizeEndpointAccessRequest authorizeRequest = AuthorizeEndpointAccessRequest.builder()
                        .vpcIds(Collections.singleton("vpc-to-be-auth"))
                        .build();
                RevokeEndpointAccessRequest revokeRequest = RevokeEndpointAccessRequest.builder()
                        .vpcIds(Collections.singleton("vpc-to-be-revoke"))
                        .build();
                DescribeEndpointAuthorizationRequest describeRequest = DescribeEndpointAuthorizationRequest.builder()
                        .build();

                mockedTranslator.when(() -> Translator.translateToUpdateRevokeRequest(any(), any(), anyBoolean()))
                        .thenReturn(revokeRequest);
                mockedTranslator.when(() -> Translator.translateToUpdateAuthorizeRequest(any(), any()))
                        .thenReturn(authorizeRequest);
                mockedTranslator.when(() -> Translator.translateToReadRequest(any())).thenReturn(describeRequest);
                mockedTranslator.when(() -> Translator.translateFromReadResponse(any())).thenReturn(model);
                mockedValidator.when(() -> Validator.doesExist(any())).thenReturn(true);

                when(proxyClient.client().revokeEndpointAccess(any(RevokeEndpointAccessRequest.class)))
                        .thenReturn(RevokeEndpointAccessResponse.builder().build());
                when(proxyClient.client().authorizeEndpointAccess(any(AuthorizeEndpointAccessRequest.class)))
                        .thenReturn(AuthorizeEndpointAccessResponse.builder().build());
                when(proxyClient.client().describeEndpointAuthorization(
                        any(DescribeEndpointAuthorizationRequest.class))
                ).thenReturn(DescribeEndpointAuthorizationResponse.builder().build());

                final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(
                        proxy, request, new CallbackContext(), proxyClient, logger
                );

                assertThat(response).isNotNull();
                assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
                assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
                assertThat(response.getResourceModels()).isNull();
                assertThat(response.getMessage()).isNull();
                assertThat(response.getErrorCode()).isNull();
            }
        }
    }
}
