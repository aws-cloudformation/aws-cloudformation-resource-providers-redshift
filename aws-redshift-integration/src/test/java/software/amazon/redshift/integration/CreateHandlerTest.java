package software.amazon.redshift.integration;

import java.time.Duration;
import java.util.Objects;

import lombok.Getter;
import org.mockito.ArgumentMatchers;
import software.amazon.awssdk.services.redshift.model.Cluster;
import software.amazon.awssdk.services.redshift.model.CreateIntegrationRequest;
import software.amazon.awssdk.services.redshift.model.CreateIntegrationResponse;
import software.amazon.awssdk.services.redshift.model.DescribeClustersRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClustersResponse;
import software.amazon.awssdk.services.redshift.model.CreateIntegrationRequest;
import software.amazon.awssdk.services.redshift.model.CreateIntegrationResponse;
import software.amazon.awssdk.services.redshift.model.DescribeIntegrationsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeIntegrationsResponse;
import software.amazon.awssdk.services.redshift.model.IntegrationAlreadyExistsException;
import software.amazon.awssdk.services.redshift.model.IntegrationConflictOperationException;
import software.amazon.awssdk.services.redshift.model.ZeroETLIntegrationStatus;
import software.amazon.cloudformation.exceptions.CfnNotStabilizedException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest extends AbstractTestBase {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private ProxyClient<RedshiftClient> proxyClient;

    @Getter
    private CreateHandler handler;

    @Mock
    RedshiftClient RedshiftClient;

    @BeforeEach
    public void setup() {
        handler = new CreateHandler();
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        RedshiftClient = mock(RedshiftClient.class);
        proxyClient = MOCK_PROXY(proxy, RedshiftClient);
    }

    @AfterEach
    public void tear_down() {
        verify(RedshiftClient, atLeastOnce()).serviceName();
    }

    @Test
    public void handleRequest_CreateIntegration_withAllFields_success() {
        when(proxyClient.client().createIntegration(any(CreateIntegrationRequest.class)))
                .thenReturn(CreateIntegrationResponse.builder()
                        .integrationArn(INTEGRATION_ARN)
                        .status(ZeroETLIntegrationStatus.ACTIVE)
                        .build());
        when(proxyClient.client().describeIntegrations(any(DescribeIntegrationsRequest.class)))
                .thenReturn(DescribeIntegrationsResponse.builder()
                        .integrations(INTEGRATION_ACTIVE)
                        .build());
        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class)))
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(Cluster.builder().clusterNamespaceArn(TARGET_ARN).clusterStatus("available").clusterAvailabilityStatus("available").build())
                        .build());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(INTEGRATION_ACTIVE_MODEL)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        verify(proxyClient.client(), times(1)).createIntegration(
                ArgumentMatchers.<CreateIntegrationRequest>argThat(req -> {
                    // TODO verify the content
                    return true;
                })
        );
        verify(proxyClient.client(), times(2)).describeIntegrations(
                ArgumentMatchers.<DescribeIntegrationsRequest>argThat(req ->
                        Objects.equals(INTEGRATION_CREATING.integrationArn(), req.integrationArn())
                )
        );
    }

    @Test
    public void handleRequest_CreateIntegration_withNoName_shouldGenerateName() {
        when(proxyClient.client().createIntegration(any(CreateIntegrationRequest.class)))
                .thenReturn(CreateIntegrationResponse.builder()
                        .integrationArn(INTEGRATION_ARN)
                        .status(ZeroETLIntegrationStatus.ACTIVE)
                        .build());
        when(proxyClient.client().describeIntegrations(any(DescribeIntegrationsRequest.class)))
                .thenReturn(DescribeIntegrationsResponse.builder()
                        .integrations(INTEGRATION_ACTIVE)
                        .build());
        when(proxyClient.client().describeClusters(any(DescribeClustersRequest.class)))
                .thenReturn(DescribeClustersResponse.builder()
                        .clusters(Cluster.builder().clusterNamespaceArn(TARGET_ARN).clusterStatus("available").clusterAvailabilityStatus("available").build())
                        .build());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(INTEGRATION_MODEL_WITH_NO_NAME)
                .stackId(STACK_ID)
                .logicalResourceIdentifier(LOGICAL_RESOURCE_IDENTIFIER)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        verify(proxyClient.client(), times(1)).createIntegration(
                ArgumentMatchers.<CreateIntegrationRequest>argThat(req -> req.integrationName().contains(LOGICAL_RESOURCE_IDENTIFIER) &&
                        req.integrationName().contains(STACK_ID))
        );
        verify(proxyClient.client(), times(2)).describeIntegrations(
                ArgumentMatchers.<DescribeIntegrationsRequest>argThat(req ->
                        Objects.equals(INTEGRATION_CREATING.integrationArn(), req.integrationArn())
                )
        );
    }

    @Test
    public void handleRequest_CreateIntegration_withTerminalFailureState_returnFailure() {
        when(proxyClient.client().createIntegration(any(CreateIntegrationRequest.class)))
                .thenReturn(CreateIntegrationResponse.builder()
                        .build());
        when(proxyClient.client().describeIntegrations(any(DescribeIntegrationsRequest.class)))
                .thenReturn(DescribeIntegrationsResponse.builder()
                        .integrations(INTEGRATION_FAILED)
                        .build());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(INTEGRATION_ACTIVE_MODEL)
                .build();

        assertThrows(CfnNotStabilizedException.class, () -> {
            handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        });
    }

    @Test
    public void handleRequest_CreateIntegration_withIntegrationAlreadyExistsException_returnFailure() {
        final String testFailMessage = "Already exist error";
        when(proxyClient.client().createIntegration(any(CreateIntegrationRequest.class)))
                .thenThrow(IntegrationAlreadyExistsException.builder()
                        .message(testFailMessage)
                        .build());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(INTEGRATION_ACTIVE_MODEL)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        assertEquals(response.getStatus(), OperationStatus.FAILED);
        assertEquals(response.getMessage(), testFailMessage);
        assertEquals(response.getErrorCode(), HandlerErrorCode.AlreadyExists);
    }

    @Test
    public void handleRequest_CreateIntegration_withDuplicateIntegrationName_returnFailure() {
        final String duplicateErrorMessage = "duplicate error";
        when(proxyClient.client().createIntegration(any(CreateIntegrationRequest.class)))
                .thenThrow(IntegrationConflictOperationException.builder()
                        .message(duplicateErrorMessage)
                        .build());

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(INTEGRATION_ACTIVE_MODEL)
                .build();

        final ProgressEvent<ResourceModel, CallbackContext> response = handler.handleRequest(proxy, request, new CallbackContext(), proxyClient, logger);
        assertEquals(response.getStatus(), OperationStatus.FAILED);
        assertEquals(response.getMessage(), duplicateErrorMessage);
        assertEquals(response.getErrorCode(), HandlerErrorCode.AlreadyExists);
    }
}
