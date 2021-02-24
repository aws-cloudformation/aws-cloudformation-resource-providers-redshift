package software.amazon.redshift.endpointauthorization;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.time.Duration;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends AbstractTestBase {
    private UpdateHandler handler;

    @Mock
    private RedshiftClient sdkClient;

    private AmazonWebServicesClientProxy proxy;
    private ProxyClient<RedshiftClient> proxyClient;
    private CallbackContext context;

    private static final OperationStatus STATUS = OperationStatus.SUCCESS;
    private static final int CALLBACK_DELAY_SECONDS = 0;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        proxyClient = MOCK_PROXY(proxy, sdkClient);
        handler = spy(new UpdateHandler());
        context = new CallbackContext();
    }

    @Nested
    @DisplayName("TestHandleUpdateRequests")
    class TestHandleRequest {
        @Mock
        private DeleteHandler deleteHandler;

        @Mock
        private CreateHandler createHandler;

        @BeforeEach
        public void setup() {
            MockitoAnnotations.openMocks(this);
        }

        @Test
        @DisplayName("Update:DeleteTest")
        public void testDeleteUpdate() {
            final ResourceModel model = ResourceModel.builder()
                    .revoke(true)
                    .build();

            final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                    .desiredResourceState(model)
                    .build();

            ProgressEvent<ResourceModel, CallbackContext> expectedResponse =
                    ProgressEvent.<ResourceModel, CallbackContext>builder()
                            .status(STATUS)
                            .resourceModel(model)
                            .callbackDelaySeconds(CALLBACK_DELAY_SECONDS)
                            .build();

            doReturn(deleteHandler).when(handler).getDeleteHandler();
            doReturn(expectedResponse).when(deleteHandler).handleRequest(proxy, request, context, proxyClient, logger);

            handler.handleRequest(proxy, request, context, proxyClient, logger);
            verify(deleteHandler).handleRequest(proxy, request, context, proxyClient, logger);
        }

        @Test
        @DisplayName("Update:CreateTest")
        public void testCreateUpdate() {
            final ResourceModel model = ResourceModel.builder()
                    .build();

            final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                    .desiredResourceState(model)
                    .build();

            ProgressEvent<ResourceModel, CallbackContext> expectedResponse =
                    ProgressEvent.<ResourceModel, CallbackContext>builder()
                            .status(STATUS)
                            .resourceModel(model)
                            .callbackDelaySeconds(CALLBACK_DELAY_SECONDS)
                            .build();

            doReturn(expectedResponse).when(createHandler).handleRequest(proxy, request, context, proxyClient, logger);
            doReturn(createHandler).when(handler).getCreateHandler();

            handler.handleRequest(proxy, request, context, proxyClient, logger);
            verify(createHandler).handleRequest(proxy, request, context, proxyClient, logger);

        }
    }
}
