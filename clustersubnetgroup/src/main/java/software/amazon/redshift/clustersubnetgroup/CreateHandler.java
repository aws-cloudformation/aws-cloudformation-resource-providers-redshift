package software.amazon.redshift.clustersubnetgroup;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetGroupAlreadyExistsException;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetGroupQuotaExceededException;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetQuotaExceededException;
import software.amazon.awssdk.services.redshift.model.CreateClusterSubnetGroupRequest;
import software.amazon.awssdk.services.redshift.model.DependentServiceRequestThrottlingException;
import software.amazon.awssdk.services.redshift.model.InvalidSubnetException;
import software.amazon.awssdk.services.redshift.model.InvalidTagException;
import software.amazon.awssdk.services.redshift.model.SubnetAlreadyInUseException;
import software.amazon.awssdk.services.redshift.model.Tag;
import software.amazon.awssdk.services.redshift.model.TagLimitExceededException;
import software.amazon.awssdk.services.redshift.model.UnauthorizedOperationException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.exceptions.CfnThrottlingException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.resource.IdentifierUtils;

import java.util.List;

import static software.amazon.redshift.clustersubnetgroup.Translator.translateTagsToSdk;

public class CreateHandler extends BaseHandler<CallbackContext> {
    private static final int MAX_SUBNET_GROUP_NAME_LENGTH = 256;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final RedshiftClient client = ClientBuilder.getClient();

        // TODO : put your code here

        model.setSubnetGroupName(
                IdentifierUtils.generateResourceIdentifier(
                        request.getLogicalResourceIdentifier(),
                        request.getClientRequestToken(),
                        MAX_SUBNET_GROUP_NAME_LENGTH
                ).toLowerCase()
        );

        final List<Tag> tags = translateTagsToSdk(request.getDesiredResourceTags(), request.getDesiredResourceState().getTags());
        CreateClusterSubnetGroupRequest createRequest = Translator.createClusterSubnetGroupRequest(model, tags);
        try {
            proxy.injectCredentialsAndInvokeV2(createRequest, client::createClusterSubnetGroup);
            logger.log(String.format("%s [%s] Created Successfully", ResourceModel.TYPE_NAME, model.getSubnetGroupName()));
        } catch (ClusterSubnetGroupAlreadyExistsException e) {
            throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, model.getSubnetGroupName());
        } catch (InvalidSubnetException | SubnetAlreadyInUseException | UnauthorizedOperationException
                | TagLimitExceededException | InvalidTagException | DependentServiceRequestThrottlingException e) {
            throw new CfnInvalidRequestException(createRequest.toString(), e);
        } catch (ClusterSubnetQuotaExceededException | ClusterSubnetGroupQuotaExceededException e ) {
            throw new CfnServiceLimitExceededException(ResourceModel.TYPE_NAME, e.toString());
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
            .resourceModel(model)
            .status(OperationStatus.SUCCESS)
            .build();
    }
}
