package software.amazon.redshift.clustersubnetgroup;

import java.util.List;
import java.util.Objects;

import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetGroupAlreadyExistsException;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetGroupQuotaExceededException;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetQuotaExceededException;
import software.amazon.awssdk.services.redshift.model.CreateClusterSubnetGroupRequest;
import software.amazon.awssdk.services.redshift.model.CreateClusterSubnetGroupResponse;
import software.amazon.awssdk.services.redshift.model.DependentServiceRequestThrottlingException;
import software.amazon.awssdk.services.redshift.model.InvalidSubnetException;
import software.amazon.awssdk.services.redshift.model.InvalidTagException;
import software.amazon.awssdk.services.redshift.model.SubnetAlreadyInUseException;
import software.amazon.awssdk.services.redshift.model.Tag;
import software.amazon.awssdk.services.redshift.model.TagLimitExceededException;
import software.amazon.awssdk.services.redshift.model.UnauthorizedOperationException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.resource.IdentifierUtils;



public class CreateHandler extends BaseHandlerStd {
    private Logger logger;
    private static final int MAX_SUBNET_GROUP_NAME_LENGTH = 256;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<RedshiftClient> proxyClient,
        final Logger logger) {

        this.logger = logger;

        final ResourceModel model = request.getDesiredResourceState();
        model.setSubnetGroupName(
                IdentifierUtils.generateResourceIdentifier(
                        request.getLogicalResourceIdentifier(),
                        request.getClientRequestToken(),
                        MAX_SUBNET_GROUP_NAME_LENGTH
                ).toLowerCase()
        );

        //final List<Tag> tags = Translator.translateTagsToSdk(request.getDesiredResourceTags(), request.getDesiredResourceState().getTags());
        //CreateClusterSubnetGroupRequest createRequest = Translator.translateToCreateRequest(model, tags);

        return ProgressEvent.progress(model, callbackContext)
                .then(progress -> proxy.initiate("AWS-Redshift-ClusterSubnetGroup::Create", proxyClient, model, callbackContext)
                        .translateToServiceRequest((m) -> Translator.translateToCreateRequest(model, request.getDesiredResourceTags()))
                        .makeServiceCall(this::createResource)
                        .progress())
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private CreateClusterSubnetGroupResponse createResource(
        final CreateClusterSubnetGroupRequest createRequest,
        final ProxyClient<RedshiftClient> proxyClient) {
        CreateClusterSubnetGroupResponse createResponse = null;

        try {
            createResponse = proxyClient.injectCredentialsAndInvokeV2(createRequest, proxyClient.client()::createClusterSubnetGroup);
        } catch (final ClusterSubnetGroupAlreadyExistsException e) {
            throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, createRequest.clusterSubnetGroupName());
        } catch (final InvalidSubnetException | SubnetAlreadyInUseException | UnauthorizedOperationException
                | TagLimitExceededException | InvalidTagException | DependentServiceRequestThrottlingException e) {
            throw new CfnInvalidRequestException(createRequest.toString(), e);
        } catch (final ClusterSubnetQuotaExceededException | ClusterSubnetGroupQuotaExceededException e) {
            throw new CfnServiceLimitExceededException(ResourceModel.TYPE_NAME, e.toString());
        }

        logger.log(String.format("%s successfully created.", ResourceModel.TYPE_NAME));
        return createResponse;
    }
}
