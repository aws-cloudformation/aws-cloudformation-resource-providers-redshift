package software.amazon.redshift.clustersubnetgroup;

import software.amazon.awssdk.services.redshift.model.ClusterSubnetGroup;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetGroupAlreadyExistsException;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetGroupQuotaExceededException;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetQuotaExceededException;
import software.amazon.awssdk.services.redshift.model.CreateClusterSubnetGroupRequest;
import software.amazon.awssdk.services.redshift.model.CreateClusterSubnetGroupResponse;
import software.amazon.awssdk.services.redshift.model.DependentServiceRequestThrottlingException;
import software.amazon.awssdk.services.redshift.model.InvalidSubnetException;
import software.amazon.awssdk.services.redshift.model.InvalidTagException;
import software.amazon.awssdk.services.redshift.model.Subnet;
import software.amazon.awssdk.services.redshift.model.SubnetAlreadyInUseException;
import software.amazon.awssdk.services.redshift.model.TagLimitExceededException;
import software.amazon.awssdk.services.redshift.model.UnauthorizedOperationException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static software.amazon.redshift.clustersubnetgroup.TestUtils.AWS_REGION;
import static software.amazon.redshift.clustersubnetgroup.TestUtils.BASIC_MODEL;
import static software.amazon.redshift.clustersubnetgroup.TestUtils.DESIRED_RESOURCE_TAGS;

@ExtendWith(MockitoExtension.class)
public class CreateHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    private CreateHandler handler;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        handler = new CreateHandler();
    }

    @Test
    public void handleRequest_SimpleSuccess() {
        final ResourceModel model = BASIC_MODEL;

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .region(AWS_REGION)
                .desiredResourceTags(DESIRED_RESOURCE_TAGS)
                .logicalResourceIdentifier("logicalId")
                .clientRequestToken("token")
                .build();

        doReturn(CreateClusterSubnetGroupResponse.builder()
                .clusterSubnetGroup(ClusterSubnetGroup.builder()
                        .clusterSubnetGroupName("name")
                        .description("description")
                        .subnets(Subnet.builder().subnetIdentifier("subnet").build(), Subnet.builder().subnetIdentifier("subnet").build())
                        .build())
                .build()).when(proxy).injectCredentialsAndInvokeV2(any(CreateClusterSubnetGroupRequest.class), any());


        final ProgressEvent<ResourceModel, CallbackContext> response
            = handler.handleRequest(proxy, request, null, logger);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(OperationStatus.SUCCESS);
        assertThat(response.getCallbackContext()).isNull();
        assertThat(response.getCallbackDelaySeconds()).isEqualTo(0);
        assertThat(response.getResourceModel()).isEqualTo(request.getDesiredResourceState());
        assertThat(response.getResourceModels()).isNull();
        assertThat(response.getMessage()).isNull();
        assertThat(response.getErrorCode()).isNull();
    }

    @Test
    public void handleRequest_ClusterSubnetGroupAlreadyExistsException() {
        doThrow(ClusterSubnetGroupAlreadyExistsException.class)
                .when(proxy).injectCredentialsAndInvokeV2(any(), any());

        final ResourceModel model = BASIC_MODEL;

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .logicalResourceIdentifier("logicalId")
                .clientRequestToken("token")
                .build();

        assertThrows(CfnAlreadyExistsException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_InvalidSubnetException() {
        doThrow(InvalidSubnetException.class)
                .when(proxy).injectCredentialsAndInvokeV2(any(), any());

        final ResourceModel model = BASIC_MODEL;

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .logicalResourceIdentifier("logicalId")
                .clientRequestToken("token")
                .build();

        assertThrows(CfnInvalidRequestException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_SubnetAlreadyInUseException() {
        doThrow(SubnetAlreadyInUseException.class)
                .when(proxy).injectCredentialsAndInvokeV2(any(), any());

        final ResourceModel model = BASIC_MODEL;

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .logicalResourceIdentifier("logicalId")
                .clientRequestToken("token")
                .build();

        assertThrows(CfnInvalidRequestException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_UnauthorizedOperationException() {
        doThrow(UnauthorizedOperationException.class)
                .when(proxy).injectCredentialsAndInvokeV2(any(), any());

        final ResourceModel model = BASIC_MODEL;

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .logicalResourceIdentifier("logicalId")
                .clientRequestToken("token")
                .build();

        assertThrows(CfnInvalidRequestException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_TagLimitExceededException() {
        doThrow(TagLimitExceededException.class)
                .when(proxy).injectCredentialsAndInvokeV2(any(), any());

        final ResourceModel model = BASIC_MODEL;

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .logicalResourceIdentifier("logicalId")
                .clientRequestToken("token")
                .build();

        assertThrows(CfnInvalidRequestException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_InvalidTagException() {
        doThrow(InvalidTagException.class)
                .when(proxy).injectCredentialsAndInvokeV2(any(), any());

        final ResourceModel model = BASIC_MODEL;

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .logicalResourceIdentifier("logicalId")
                .clientRequestToken("token")
                .build();

        assertThrows(CfnInvalidRequestException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_DependentServiceRequestThrottlingException() {
        doThrow(DependentServiceRequestThrottlingException.class)
                .when(proxy).injectCredentialsAndInvokeV2(any(), any());

        final ResourceModel model = BASIC_MODEL;

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .logicalResourceIdentifier("logicalId")
                .clientRequestToken("token")
                .build();

        assertThrows(CfnInvalidRequestException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_ClusterSubnetQuotaExceededException() {
        doThrow(ClusterSubnetQuotaExceededException.class)
                .when(proxy).injectCredentialsAndInvokeV2(any(), any());

        final ResourceModel model = BASIC_MODEL;

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .logicalResourceIdentifier("logicalId")
                .clientRequestToken("token")
                .build();

        assertThrows(CfnServiceLimitExceededException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_ClusterSubnetGroupQuotaExceededException() {
        doThrow(ClusterSubnetGroupQuotaExceededException.class)
                .when(proxy).injectCredentialsAndInvokeV2(any(), any());

        final ResourceModel model = BASIC_MODEL;

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .logicalResourceIdentifier("logicalId")
                .clientRequestToken("token")
                .build();

        assertThrows(CfnServiceLimitExceededException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }
}
