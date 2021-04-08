package software.amazon.redshift.endpointaccess;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.CreateEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.CreateEndpointAccessResponse;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAccessResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
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

    @AfterEach
    public void tear_down() {
        verify(sdkClient, atLeastOnce()).serviceName();
        verifyNoMoreInteractions(sdkClient);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final CreateHandler handler = new CreateHandler();

        final ResourceModel model = ResourceModel.builder()
                .build();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
            .desiredResourceState(model)
            .build();

        try (MockedStatic<Translator> mockedTranslator = Mockito.mockStatic(Translator.class)) {
            // Mock the interactions with the translator, which is used by both the create and read handlers
            CreateEndpointAccessRequest createRequest = CreateEndpointAccessRequest.builder().build();
            DescribeEndpointAccessRequest describeRequest = DescribeEndpointAccessRequest.builder().build();
            mockedTranslator.when(() -> Translator.translateToCreateRequest(model)).thenReturn(createRequest);
            mockedTranslator.when(() -> Translator.translateToReadRequest(model)).thenReturn(describeRequest);
            mockedTranslator.when(() -> Translator.translateFromReadResponse(any(DescribeEndpointAccessResponse.class)))
                    .thenReturn(model);

            try (MockedStatic<EndpointAccessStabilizers> mockedStabilizers =
                         Mockito.mockStatic(EndpointAccessStabilizers.class)) {
                // Mock the interactions with the stabilizers
                mockedStabilizers.when(() -> EndpointAccessStabilizers.isEndpointActive(any(), any(), any()))
                        .thenReturn(true);

                // Mock the interactions with the proxy client
                when(proxyClient.client().createEndpointAccess(any(CreateEndpointAccessRequest.class)))
                        .thenReturn(CreateEndpointAccessResponse.builder()
                                .build());

                when(proxyClient.client().describeEndpointAccess(any(DescribeEndpointAccessRequest.class)))
                        .thenReturn(DescribeEndpointAccessResponse.builder()
                                .build());

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
    }
}
