package software.amazon.redshift.clusterparametergroup;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.*;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.resource.IdentifierUtils;

import java.util.Random;
import java.util.UUID;

public class CreateHandler extends BaseHandlerStd {
    private Logger logger;
    private static final int MAX_PARAMETER_GROUP_NAME_LENGTH = 255;
    Random random = new Random();

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<RedshiftClient> proxyClient,
            final Logger logger) {

        this.logger = logger;

        prepareResourceModel(request);
        final ResourceModel model = request.getDesiredResourceState();
        return ProgressEvent.progress(model, callbackContext)
                .then(progress -> proxy.initiate("AWS-Redshift-ClusterParameterGroup::Create", proxyClient, model, callbackContext)
                        .translateToServiceRequest((resourceModel) -> Translator.translateToCreateRequest(resourceModel, request.getDesiredResourceTags()))
                        .makeServiceCall((awsRequest, client) -> {
                            CreateClusterParameterGroupResponse awsResponse;
                            try {
                                awsResponse = client.injectCredentialsAndInvokeV2(awsRequest, client.client()::createClusterParameterGroup);
                            } catch (final ClusterParameterGroupAlreadyExistsException e) {
                                throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, awsRequest.parameterGroupName());
                            } catch (final ClusterParameterGroupQuotaExceededException e) {
                                throw new CfnServiceLimitExceededException(ResourceModel.TYPE_NAME, e.toString());
                            } catch (final AwsServiceException e) {
                                logger.log(String.format("%s [%s] Created failed", ResourceModel.TYPE_NAME,
                                        request.getDesiredResourceState().getParameterGroupName()));
                                throw new CfnInvalidRequestException(awsRequest.toString(), e);
                            }
                            logger.log(String.format("%s [%s] Created Successfully", ResourceModel.TYPE_NAME,
                                    request.getDesiredResourceState().getParameterGroupName()));
                            return awsResponse;
                        })
                        .done((paramGroupRequest, paramGroupResponse, proxyInvocation, resourceModel, context) -> applyParameters(proxy, proxyInvocation, resourceModel, context, logger))
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

        if (StringUtils.isNullOrEmpty(model.getParameterGroupName())) {
            // make sure it starts with an alphabet as some resources require ID start with a letter
            String logicalResourceIdentifier = StringUtils.isNullOrEmpty(request.getLogicalResourceIdentifier())
                    ? UUID.randomUUID().toString() : request.getLogicalResourceIdentifier();
            logicalResourceIdentifier = (char)(random.nextInt(26) + 'a') + logicalResourceIdentifier;
            model.setParameterGroupName(
                    IdentifierUtils.generateResourceIdentifier(
                            logicalResourceIdentifier,
                            request.getClientRequestToken(),
                            MAX_PARAMETER_GROUP_NAME_LENGTH
                    ).toLowerCase()
            );
        }
    }
}
