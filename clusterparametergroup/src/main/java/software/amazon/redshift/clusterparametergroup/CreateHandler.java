package software.amazon.redshift.clusterparametergroup;

// TODO: replace all usage of SdkClient with your service client type, e.g; YourServiceAsyncClient
// import software.amazon.awssdk.services.yourservice.YourServiceAsyncClient;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.*;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.resource.IdentifierUtils;


public class CreateHandler extends BaseHandlerStd {
    private Logger logger;
    private static final int MAX_PARAMETER_GROUP_NAME_LENGTH = 255;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<RedshiftClient> proxyClient,
        final Logger logger) {

        this.logger = logger;

        // TODO: Adjust Progress Chain according to your implementation
        // https://github.com/aws-cloudformation/cloudformation-cli-java-plugin/blob/master/src/main/java/software/amazon/cloudformation/proxy/CallChain.java
        prepareResourceModel(request);
        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
            // STEP 2 [create/stabilize progress chain - required for resource creation]
            .then(progress ->
                proxy.initiate("AWS-Redshift-ClusterParameterGroup::Create", proxyClient,progress.getResourceModel(), progress.getCallbackContext())
                    .translateToServiceRequest((m) -> Translator.translateToCreateRequest(request.getDesiredResourceState(), request.getDesiredResourceTags()))
                    .makeServiceCall((awsRequest, client) -> {
                        CreateClusterParameterGroupResponse awsResponse;
                        try {
                            awsResponse = client.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::createClusterParameterGroup);
                        } catch (final ClusterParameterGroupAlreadyExistsException e) {
                            throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, awsRequest.parameterGroupName());
                        } catch (final TagLimitExceededException | InvalidTagException e) {
                            throw new CfnInvalidRequestException(awsRequest.toString(), e);
                        } catch (final ClusterParameterGroupQuotaExceededException | ClusterSubnetGroupQuotaExceededException e) {
                            throw new CfnServiceLimitExceededException(ResourceModel.TYPE_NAME, e.toString());
                        }
                        logger.log(String.format("%s [%s] Created Successfully", ResourceModel.TYPE_NAME,
                                request.getDesiredResourceState().getParameterGroupName()));
                        return awsResponse;
                    })
                    .progress()
                )

            // STEP 3 [TODO: describe call/chain to return the resource model]
            .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }
    /**
        Generate a ID for request if it doesn't have one
     */
    private void prepareResourceModel(ResourceHandlerRequest<ResourceModel> request) {
        if (request.getDesiredResourceState() == null) {
            request.setDesiredResourceState(new ResourceModel());
        }
        final ResourceModel model = request.getDesiredResourceState();

        if (StringUtils.isNullOrEmpty(model.getParameterGroupName())) {
            model.setParameterGroupName(
                    IdentifierUtils.generateResourceIdentifier(
                            request.getLogicalResourceIdentifier(),
                            request.getClientRequestToken(),
                            MAX_PARAMETER_GROUP_NAME_LENGTH
                    ).toLowerCase()
            );
        }
    }

}