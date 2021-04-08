package software.amazon.redshift.endpointaccess;

import lombok.NonNull;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterNotFoundException;
import software.amazon.awssdk.services.redshift.model.DeleteEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.DeleteEndpointAccessResponse;
import software.amazon.awssdk.services.redshift.model.EndpointNotFoundException;
import software.amazon.awssdk.services.redshift.model.InvalidEndpointStateException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
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

        ResourceModel resourceModel = request.getDesiredResourceState();

        Validator.validateDeleteRequest(resourceModel);

        return ProgressEvent.progress(resourceModel, callbackContext)
                .then(progress -> proxy.initiate("AWS-Redshift-EndpointAccess::Delete",
                        proxyClient,
                        progress.getResourceModel(),
                        progress.getCallbackContext())
                        .translateToServiceRequest(Translator::translateToDeleteRequest)
                        .makeServiceCall(this::deleteEndpointAccess)
                        .stabilize((_request, _response, client, model, ctx) ->
                                EndpointAccessStabilizers.isEndpointDeleted(client, model, ctx))
                        .done(response -> ProgressEvent.defaultSuccessHandler(null))
            );
    }

    private DeleteEndpointAccessResponse deleteEndpointAccess(
            @NonNull final DeleteEndpointAccessRequest deleteRequest,
            @NonNull final ProxyClient<RedshiftClient> proxyClient) {

        DeleteEndpointAccessResponse response;

        try {
            response = proxyClient.injectCredentialsAndInvokeV2(
                    deleteRequest, proxyClient.client()::deleteEndpointAccess
            );
        } catch (EndpointNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, deleteRequest.endpointName(), e);
        } catch (InvalidEndpointStateException e) {
            throw new CfnResourceConflictException(
                    ResourceModel.TYPE_NAME,
                    deleteRequest.endpointName(),
                    "Endpoint status must be active",
                    e
            );
        } catch (ClusterNotFoundException e) {
              throw new CfnInvalidRequestException(deleteRequest.toString(), e);
        } catch (Exception e) { // InvalidClusterSecurityGroupStateFault, InvalidClusterStateFault
            throw new CfnGeneralServiceException(deleteRequest.toString(), e);
        }

        return response;
    }
}
