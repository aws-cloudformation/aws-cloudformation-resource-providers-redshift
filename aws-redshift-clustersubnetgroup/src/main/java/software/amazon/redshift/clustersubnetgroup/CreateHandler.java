package software.amazon.redshift.clustersubnetgroup;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetGroupAlreadyExistsException;
import software.amazon.awssdk.services.redshift.model.CreateClusterSubnetGroupRequest;
import software.amazon.awssdk.services.redshift.model.CreateClusterSubnetGroupResponse;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.resource.IdentifierUtils;

import java.util.UUID;

public class CreateHandler extends BaseHandlerStd {

    private Logger logger;
    private static final int MAX_SUBNET_GROUP_NAME_LENGTH = 255;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<RedshiftClient> proxyClient,
            final Logger logger) {

        this.logger = logger;
        final ResourceModel model = request.getDesiredResourceState();

        return ProgressEvent.progress(model, callbackContext)
                .then(progress -> proxy.initiate("AWS-Redshift-ClusterSubnetGroup::Create", proxyClient, model, callbackContext)
                        .translateToServiceRequest((m) -> Translator.translateToCreateRequest(generateSubnetGroupName(request),
                                m, request.getDesiredResourceTags()))
                        .makeServiceCall(this::createResource)
                        .handleError((createDbSubnetGroupRequest, exception, client, resourceModel, cxt) -> {
                            if (exception instanceof ClusterSubnetGroupAlreadyExistsException) {
                                return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.AlreadyExists);
                            }
                            throw exception;
                        })
                        .progress())
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private CreateClusterSubnetGroupResponse createResource(
            final CreateClusterSubnetGroupRequest createRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        CreateClusterSubnetGroupResponse createResponse = null;

        createResponse = proxyClient.injectCredentialsAndInvokeV2(createRequest, proxyClient.client()::createClusterSubnetGroup);
        logger.log(String.format("%s successfully created.", ResourceModel.TYPE_NAME));
        return createResponse;
    }

    private String generateSubnetGroupName(ResourceHandlerRequest<ResourceModel> request) {
        final String logicalResourceIdentifier = StringUtils.isNullOrEmpty(request.getLogicalResourceIdentifier())
                ? UUID.randomUUID().toString() : request.getLogicalResourceIdentifier();

        return IdentifierUtils.generateResourceIdentifier(
                logicalResourceIdentifier,
                request.getClientRequestToken(),
                MAX_SUBNET_GROUP_NAME_LENGTH
        ).toLowerCase();
    }
}
