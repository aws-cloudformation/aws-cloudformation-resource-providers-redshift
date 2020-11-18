package software.amazon.redshift.clustersecuritygroup;

// TODO: replace all usage of SdkClient with your service client type, e.g; YourServiceAsyncClient
// import software.amazon.awssdk.services.yourservice.YourServiceAsyncClient;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterSecurityGroupAlreadyExistsException;
import software.amazon.awssdk.services.redshift.model.CreateClusterSecurityGroupResponse;
import software.amazon.awssdk.services.redshift.model.InvalidTagException;
import software.amazon.awssdk.services.redshift.model.TagLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
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
                        proxy.initiate("AWS-Redshift-ClusterSecurityGroup::Create", proxyClient, progress.getResourceModel(), progress.getCallbackContext())
                                .translateToServiceRequest((resourceModel) -> Translator.translateToCreateRequest(resourceModel, request.getDesiredResourceTags()))
                                .makeServiceCall((awsRequest, client) -> {
                                    CreateClusterSecurityGroupResponse awsResponse;
                                    try {
                                        awsResponse = client.injectCredentialsAndInvokeV2(awsRequest, client.client()::createClusterSecurityGroup);
                                    } catch (final ClusterSecurityGroupAlreadyExistsException e) {
                                        throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, awsRequest.toString());
                                    } catch (final TagLimitExceededException | InvalidTagException e) {
                                        throw new CfnInvalidRequestException(awsRequest.toString(), e);
                                    }

                                    logger.log(String.format("%s [%s] Created Successfully", ResourceModel.TYPE_NAME,
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

        if (StringUtils.isNullOrEmpty(model.getClusterSecurityGroupName())) {
            final String logicalResourceIdentifier = StringUtils.isNullOrEmpty(request.getLogicalResourceIdentifier())
                    ? UUID.randomUUID().toString() : request.getLogicalResourceIdentifier();
            model.setClusterSecurityGroupName(
                    IdentifierUtils.generateResourceIdentifier(
                            logicalResourceIdentifier,
                            request.getClientRequestToken(),
                            MAX_NAME_LENGTH
                    ).toLowerCase()
            );
        }
    }
}
