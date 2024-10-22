package software.amazon.redshift.integration;

import java.time.Duration;

import lombok.Getter;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.CreateTagsRequest;
import software.amazon.awssdk.services.redshift.model.CreateTagsResponse;
import software.amazon.awssdk.services.redshift.model.DeleteTagsRequest;
import software.amazon.awssdk.services.redshift.model.DeleteTagsResponse;
import software.amazon.awssdk.services.redshift.model.DescribeClustersRequest;
import software.amazon.awssdk.services.redshift.model.DescribeIntegrationsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeIntegrationsResponse;
import software.amazon.awssdk.services.redshift.model.ModifyIntegrationRequest;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<RedshiftClient> proxyClient;

    @Mock
    RedshiftClient RedshiftClient;

    @Getter
    private UpdateHandler handler;

    @BeforeEach
    public void setup() {
        handler = new UpdateHandler();
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        RedshiftClient = mock(RedshiftClient.class);
        proxyClient = MOCK_PROXY(proxy, RedshiftClient);
    }

    @AfterEach
    public void tear_down() {
        verify(RedshiftClient, atLeastOnce()).serviceName();
    }

    @Test
    public void handleRequest_updateTags() {
        when(proxyClient.client().deleteTags(any(DeleteTagsRequest.class)))
                .thenReturn(DeleteTagsResponse.builder().build());
        when(proxyClient.client().createTags(any(CreateTagsRequest.class)))
                .thenReturn(CreateTagsResponse.builder().build());
        when(proxyClient.client().describeIntegrations(any(DescribeIntegrationsRequest.class)))
                .thenReturn(DescribeIntegrationsResponse.builder().integrations(INTEGRATION_ACTIVE).build());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceState(INTEGRATION_ACTIVE_MODEL.toBuilder().tags(TAG_LIST).build())
                .desiredResourceState(INTEGRATION_ACTIVE_MODEL.toBuilder().tags(TAG_LIST_ALTER).build())
                .previousResourceTags(Tagging.translateTagsToRequest(TAG_LIST))
                .desiredResourceTags(Tagging.translateTagsToRequest(TAG_LIST_ALTER))
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        verify(proxyClient.client(), times(2)).describeIntegrations(any(DescribeIntegrationsRequest.class));
        verify(proxyClient.client(), times(1)).deleteTags(any(DeleteTagsRequest.class));
        verify(proxyClient.client(), times(1)).createTags(any(CreateTagsRequest.class));
        assertThat(response).isNotNull();
    }

    @Test
    public void handleRequest_updateIntegrationName() {
        when(proxyClient.client().describeIntegrations(any(DescribeIntegrationsRequest.class)))
                .thenReturn(DescribeIntegrationsResponse.builder().integrations(INTEGRATION_ACTIVE).build());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .previousResourceState(INTEGRATION_ACTIVE_MODEL.toBuilder().integrationName("old").build())
                .desiredResourceState(INTEGRATION_ACTIVE_MODEL.toBuilder().integrationName("new").build())
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        verify(proxyClient.client(), times(1)).describeIntegrations(any(DescribeIntegrationsRequest.class));
        verify(proxyClient.client(), times(1)).describeClusters(any(DescribeClustersRequest.class));
        verify(proxyClient.client(), times(1)).modifyIntegration(any(ModifyIntegrationRequest.class));
        assertThat(response).isNotNull();
    }
}
