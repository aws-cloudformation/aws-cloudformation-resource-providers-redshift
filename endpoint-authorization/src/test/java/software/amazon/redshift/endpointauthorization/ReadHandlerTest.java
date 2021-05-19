package software.amazon.redshift.endpointauthorization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAuthorizationRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAuthorizationResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
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

    @Test
    public void handleRequest_SimpleSuccess() {
        final ResourceModel model = ResourceModel.builder().build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        DescribeEndpointAuthorizationResponse describeResponse = DescribeEndpointAuthorizationResponse.builder()
                .build();

        when(proxyClient.client().describeEndpointAuthorization(any(DescribeEndpointAuthorizationRequest.class)))
                .thenReturn(describeResponse);

        try (MockedStatic<Translator> mockedTranslator = mockStatic(Translator.class)) {
            try (MockedStatic<Validator> mockedValidator = mockStatic(Validator.class)) {
                DescribeEndpointAuthorizationRequest describeRequest = DescribeEndpointAuthorizationRequest.builder()
                        .build();

                mockedTranslator.when(() -> Translator.translateToReadRequest(any())).thenReturn(
                        describeRequest
                );

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
