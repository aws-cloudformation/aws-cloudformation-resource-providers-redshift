package software.amazon.redshift.eventsubscription;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.CreateTagsRequest;
import software.amazon.awssdk.services.redshift.model.CreateTagsResponse;
import software.amazon.awssdk.services.redshift.model.DescribeEventSubscriptionsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeTagsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeTagsResponse;
import software.amazon.awssdk.services.redshift.model.ModifyEventSubscriptionRequest;
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

import static software.amazon.redshift.eventsubscription.TestUtils.DESIRED_RESOURCE_TAGS;
import static software.amazon.redshift.eventsubscription.TestUtils.PREVIOUS_TAGS;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends AbstractTestBase {

    @Mock
    RedshiftClient sdkClient;
    @Mock
    private AmazonWebServicesClientProxy proxy;
    @Mock
    private ProxyClient<RedshiftClient> proxyClient;

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(RedshiftClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final UpdateHandler handler = new UpdateHandler();

        final ResourceModel requestResourceModel = getUpdateRequestResourceModel();
        final ResourceModel responseResourceModel = getUpdateResponseResourceModel();

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(requestResourceModel)
                .desiredResourceTags(DESIRED_RESOURCE_TAGS)
                .previousResourceState(requestResourceModel)
                .previousResourceTags(PREVIOUS_TAGS)
                .build();

        when(proxyClient.client().describeTags(any(DescribeTagsRequest.class))).thenReturn(DescribeTagsResponse.builder().build());
        when(proxyClient.client().createTags(any(CreateTagsRequest.class))).thenReturn(CreateTagsResponse.builder().build());
        when(proxyClient.client().modifyEventSubscription(any(ModifyEventSubscriptionRequest.class))).thenReturn(getUpdateResponseSdk());
        when(proxyClient.client().describeEventSubscriptions(any(DescribeEventSubscriptionsRequest.class))).thenReturn(getReadResponseSdk());

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(responseResourceModel);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }
}
