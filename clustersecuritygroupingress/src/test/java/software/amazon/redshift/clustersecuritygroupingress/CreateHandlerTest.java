package software.amazon.redshift.clustersecuritygroupingress;

import java.time.Duration;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.AuthorizeClusterSecurityGroupIngressRequest;
import software.amazon.awssdk.services.redshift.model.AuthorizeClusterSecurityGroupIngressResponse;
import software.amazon.awssdk.services.redshift.model.ClusterSecurityGroup;
import software.amazon.awssdk.services.redshift.model.CreateClusterSecurityGroupRequest;
import software.amazon.awssdk.services.redshift.model.CreateClusterSecurityGroupResponse;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSecurityGroupsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSecurityGroupsResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.atLeastOnce;
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

    private CreateHandler handler;

    final static ResourceModel COMPLETE_MODEL = ResourceModel.builder()
            .clusterSecurityGroupName("SECURITY_GROUP_NAME")
            .cIDRIP("0.0.0./0")
            .build();

    final static String AWS_REGION = "us-east-1";

    @BeforeEach
    public void setup() {
        proxy = new AmazonWebServicesClientProxy(logger, MOCK_CREDENTIALS, () -> Duration.ofSeconds(600).toMillis());
        sdkClient = mock(RedshiftClient.class);
        proxyClient = MOCK_PROXY(proxy, sdkClient);
        handler = new CreateHandler();
    }

    @AfterEach
    public void tear_down() {
        verify(sdkClient, atLeastOnce()).serviceName();
        verifyNoMoreInteractions(sdkClient);
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(COMPLETE_MODEL)
                .region(AWS_REGION)
                .clientRequestToken("token")
                .logicalResourceIdentifier("logicalId")
                .build();

        when(proxyClient.client().authorizeClusterSecurityGroupIngress(any(AuthorizeClusterSecurityGroupIngressRequest.class)))
                .thenReturn(AuthorizeClusterSecurityGroupIngressResponse.builder()
                        .clusterSecurityGroup(ClusterSecurityGroup.builder().build())
                        .build());

        CallbackContext callbackContext = new CallbackContext();

        final ProgressEvent<ResourceModel, CallbackContext> response
                = handler.handleRequest(proxy, request, callbackContext, proxyClient, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }
}
