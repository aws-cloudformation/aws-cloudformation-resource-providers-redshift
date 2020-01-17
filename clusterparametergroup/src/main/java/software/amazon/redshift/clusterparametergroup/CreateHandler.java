package software.amazon.redshift.clusterparametergroup;

import org.apache.commons.collections.CollectionUtils;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterParameterGroupAlreadyExistsException;
import software.amazon.awssdk.services.redshift.model.InvalidClusterParameterGroupStateException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.resource.IdentifierUtils;

public class CreateHandler extends BaseHandler<CallbackContext> {
    private static final int MAX_PARAMETER_GROUP_NAME_LENGTH = 256;

    @Override
    public ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final Logger logger) {

        final ResourceModel model = request.getDesiredResourceState();
        final RedshiftClient client = ClientBuilder.getClient();

        model.setParameterGroupName(
                IdentifierUtils.generateResourceIdentifier(
                        request.getLogicalResourceIdentifier(),
                        request.getClientRequestToken(),
                        MAX_PARAMETER_GROUP_NAME_LENGTH
                ).toLowerCase()
        );

        try {
            proxy.injectCredentialsAndInvokeV2(Translator.createClusterParameterGroupRequest(model, Translator.translateTagsToSdk(request.getDesiredResourceTags())), client::createClusterParameterGroup);
            logger.log(String.format("%s [%s] Created Successfully", ResourceModel.TYPE_NAME, model.getParameterGroupName()));
        } catch (ClusterParameterGroupAlreadyExistsException e) {
            throw new CfnAlreadyExistsException(e);
        }

        if (!CollectionUtils.isEmpty(model.getParameters())) {
            try {
                proxy.injectCredentialsAndInvokeV2(Translator.modifyClusterParameterGroupRequest(model), client::modifyClusterParameterGroup);
                logger.log(String.format("Successfully applied parameters: %s", model.getParameters()));
            } catch (InvalidClusterParameterGroupStateException e) {
                throw new CfnGeneralServiceException(e);
            }
        }

        return ProgressEvent.<ResourceModel, CallbackContext>builder()
                .resourceModel(model)
                .status(OperationStatus.SUCCESS)
                .build();
    }
}
