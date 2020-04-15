package software.amazon.redshift.clustersubnetgroup;

import software.amazon.awssdk.services.redshift.model.ClusterSubnetGroup;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetQuotaExceededException;
import software.amazon.awssdk.services.redshift.model.DependentServiceRequestThrottlingException;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSubnetGroupsResponse;
import software.amazon.awssdk.services.redshift.model.DescribeTagsRequest;
import software.amazon.awssdk.services.redshift.model.InvalidSubnetException;
import software.amazon.awssdk.services.redshift.model.ModifyClusterSubnetGroupRequest;
import software.amazon.awssdk.services.redshift.model.ModifyClusterSubnetGroupResponse;
import software.amazon.awssdk.services.redshift.model.Subnet;
import software.amazon.awssdk.services.redshift.model.SubnetAlreadyInUseException;
import software.amazon.awssdk.services.redshift.model.UnauthorizedOperationException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
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
import static software.amazon.redshift.clustersubnetgroup.TestUtils.DESCRIBE_TAGS_RESPONSE;
import static software.amazon.redshift.clustersubnetgroup.TestUtils.DESIRED_RESOURCE_TAGS;

@ExtendWith(MockitoExtension.class)
public class UpdateHandlerTest {

    @Mock
    private AmazonWebServicesClientProxy proxy;

    @Mock
    private Logger logger;

    private UpdateHandler handler;

    @BeforeEach
    public void setup() {
        proxy = mock(AmazonWebServicesClientProxy.class);
        logger = mock(Logger.class);
        handler = new UpdateHandler();
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

        final DescribeClusterSubnetGroupsResponse describeClusterSubnetGroupsResponse = DescribeClusterSubnetGroupsResponse.builder()
                .clusterSubnetGroups(ClusterSubnetGroup.builder().build())
                .build();

        doReturn(ModifyClusterSubnetGroupResponse.builder()
                        .clusterSubnetGroup(ClusterSubnetGroup.builder()
                                .clusterSubnetGroupName("name")
                                .description("description")
                                .subnets(Subnet.builder().subnetIdentifier("subnet").build())
                                .build())
                        .build()).when(proxy).injectCredentialsAndInvokeV2(any(ModifyClusterSubnetGroupRequest.class), any());

        doReturn(DESCRIBE_TAGS_RESPONSE).when(proxy).injectCredentialsAndInvokeV2(any(DescribeTagsRequest.class), any());

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
    public void handleRequest_InvalidSubnetException() {
        doThrow(InvalidSubnetException.class)
                .when(proxy).injectCredentialsAndInvokeV2(any(), any());

        final ResourceModel model = BASIC_MODEL;

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
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
                .build();

        assertThrows(CfnInvalidRequestException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_DependentServiceRequestThrottlingExceptionn() {
        doThrow(DependentServiceRequestThrottlingException.class)
                .when(proxy).injectCredentialsAndInvokeV2(any(), any());

        final ResourceModel model = BASIC_MODEL;

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnInvalidRequestException.class,
                () -> handler.handleRequest(proxy, request, null, logger));    }

    @Test
    public void handleRequest_ClusterSubnetQuotaExceededException() {
        doThrow(ClusterSubnetQuotaExceededException.class)
                .when(proxy).injectCredentialsAndInvokeV2(any(), any());

        final ResourceModel model = BASIC_MODEL;

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnInvalidRequestException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

    @Test
    public void handleRequest_ClusterSubnetGroupNotFoundException() {
        doThrow(ClusterSubnetGroupNotFoundException.class)
                .when(proxy).injectCredentialsAndInvokeV2(any(), any());

        final ResourceModel model = BASIC_MODEL;

        final ResourceHandlerRequest<ResourceModel> request = ResourceHandlerRequest.<ResourceModel>builder()
                .desiredResourceState(model)
                .build();

        assertThrows(CfnNotFoundException.class,
                () -> handler.handleRequest(proxy, request, null, logger));
    }

}
