package software.amazon.redshift.clusterparametergroup;

import software.amazon.awssdk.awscore.AwsResponse;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.DeleteClusterParameterGroupResponse;
import software.amazon.awssdk.services.redshift.model.InvalidClusterSubnetGroupStateException;
import software.amazon.awssdk.services.redshift.model.InvalidClusterSubnetStateException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<RedshiftClient> proxyClient,
        final Logger logger) {

        this.logger = logger;

        // TODO: Adjust Progress Chain according to your implementation
        // https://github.com/aws-cloudformation/cloudformation-cli-java-plugin/blob/master/src/main/java/software/amazon/cloudformation/proxy/CallChain.java

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)

            // STEP 1 [check if resource already exists]
            // for more information -> https://docs.aws.amazon.com/cloudformation-cli/latest/userguide/resource-type-test-contract.html
            // if target API does not support 'ResourceNotFoundException' then following check is required
            .then(progress ->
                // STEP 1.0 [initialize a proxy context]
                // If your service API does not return ResourceNotFoundException on delete requests against some identifier (e.g; resource Name)
                // and instead returns a 200 even though a resource already deleted, you must first check if the resource exists here
                // NOTE: If your service API throws 'ResourceNotFoundException' for delete requests this method is not necessary
                proxy.initiate("AWS-Redshift-ClusterParameterGroup::Delete::PreDeletionCheck", proxyClient, progress.getResourceModel(), progress.getCallbackContext())

                    // STEP 1.1 [initialize a proxy context]
                    .translateToServiceRequest(Translator::translateToReadRequest)

                    // STEP 1.2 [TODO: make an api call]
                    .makeServiceCall((awsRequest, client) -> {
                        AwsResponse awsResponse = null;

                        // TODO: add custom read resource logic

                        logger.log(String.format("%s has successfully been read.", ResourceModel.TYPE_NAME));
                        return awsResponse;
                    })

                    // STEP 1.3 [TODO: handle exception]
                    .handleError((awsRequest, exception, client, model, context) -> {
                        // TODO: uncomment when ready to implement
                        // if (exception instanceof ResourceNotFoundException)
                        //     return ProgressEvent.success(model, context);
                        // throw exception;
                        return ProgressEvent.progress(model, context);
                    })
                    .progress()
            )

            // STEP 2.0 [delete/stabilize progress chain - required for resource deletion]
            .then(progress ->
                // If your service API throws 'ResourceNotFoundException' for delete requests then DeleteHandler can return just proxy.initiate construction
                // STEP 2.0 [initialize a proxy context]
                // Implement client invocation of the delete request through the proxyClient, which is already initialised with
                // caller credentials, correct region and retry settings
                proxy.initiate("AWS-Redshift-ClusterParameterGroup::Delete", proxyClient, progress.getResourceModel(), progress.getCallbackContext())

                    // STEP 2.1 [TODO: construct a body of a request]
                    .translateToServiceRequest(Translator::translateToDeleteRequest)

                    // STEP 2.2 [TODO: make an api call]
                    .makeServiceCall((awsRequest, client) -> {
                        DeleteClusterParameterGroupResponse awsResponse = null;
                        try {

                            // TODO: put your delete resource code here
                            awsResponse = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::deleteClusterParameterGroup);
                            logger.log(String.format("%s [%s] Deleted Successfully", ResourceModel.TYPE_NAME, awsRequest.parameterGroupName()));
                        } catch (final ClusterSubnetGroupNotFoundException e) {
                            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, awsRequest.parameterGroupName());
                        } catch (final InvalidClusterSubnetGroupStateException | InvalidClusterSubnetStateException e) {
                            throw new CfnInvalidRequestException(awsRequest.toString(), e);
                        }

                        logger.log(String.format("%s successfully deleted.", ResourceModel.TYPE_NAME));
                        return awsResponse;
                    }).success());

                    // STEP 2.3 [TODO: stabilize step is not necessarily required but typically involves describing the resource until it is in a certain status, though it can take many forms]
                    // for more information -> https://docs.aws.amazon.com/cloudformation-cli/latest/userguide/resource-type-test-contract.html
//                    .stabilize((awsRequest, awsResponse, client, model, context) -> {
//                        // TODO: put your stabilization code here
//
//                        final boolean stabilized = true;
//                        logger.log(String.format("%s [%s] deletion has stabilized: %s", ResourceModel.TYPE_NAME, model.getPrimaryIdentifier(), stabilized));
//                        return stabilized;
//                    })
//                    .progress()
//            )
//
//            // STEP 3 [TODO: return the successful progress event without resource model]
//            .then(progress -> ProgressEvent.defaultSuccessHandler(null));
    }
}
