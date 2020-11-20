package software.amazon.redshift.clustersecuritygroupingress;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.AuthorizationAlreadyExistsException;
import software.amazon.awssdk.services.redshift.model.AuthorizationQuotaExceededException;
import software.amazon.awssdk.services.redshift.model.AuthorizeClusterSecurityGroupIngressResponse;
import software.amazon.awssdk.services.redshift.model.ClusterSecurityGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.InvalidClusterSecurityGroupStateException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.resource.IdentifierUtils;

import java.util.UUID;

public class CreateHandler extends BaseHandlerStd {
    private Logger logger;
    private static final int MAX_NAME_LENGTH = 255;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<RedshiftClient> proxyClient,
            final Logger logger) {

        this.logger = logger;
        prepareResourceModel(request);

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress ->
                        proxy.initiate("AWS-Redshift-ClusterSecurityGroupIngress::Create", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                                .translateToServiceRequest(Translator::translateToCreateRequest)
                                .makeServiceCall((awsRequest, client) -> {
                                    AuthorizeClusterSecurityGroupIngressResponse awsResponse;
                                    try {
                                        awsResponse = client.injectCredentialsAndInvokeV2(awsRequest, client.client()::authorizeClusterSecurityGroupIngress);
                                    } catch (final ClusterSecurityGroupNotFoundException | InvalidClusterSecurityGroupStateException | AuthorizationAlreadyExistsException | AuthorizationQuotaExceededException e) {
                                        throw new CfnInvalidRequestException(awsRequest.toString(), e);
                                    }
                                    logger.log(String.format("%s [%s] has been authorized a new ingress rule successfully", ResourceModel.TYPE_NAME,
                                            request.getDesiredResourceState().getClusterSecurityGroupName()));
                                    return awsResponse;
                                })
                                .progress()
                )
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    /**
     * Generate a ID for request if the request doesn't have one
     */
    private void prepareResourceModel(ResourceHandlerRequest<ResourceModel> request) {
        if (request.getDesiredResourceState() == null) {
            request.setDesiredResourceState(new ResourceModel());
        }
        final ResourceModel model = request.getDesiredResourceState();

        if (StringUtils.isNullOrEmpty(model.getId())) {
            final String logicalResourceIdentifier = StringUtils.isNullOrEmpty(request.getLogicalResourceIdentifier())
                    ? UUID.randomUUID().toString() : request.getLogicalResourceIdentifier();
            model.setId(
                    IdentifierUtils.generateResourceIdentifier(
                            logicalResourceIdentifier,
                            request.getClientRequestToken(),
                            MAX_NAME_LENGTH
                    ).toLowerCase()
            );
        }
    }
}
