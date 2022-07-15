package software.amazon.redshift.endpointauthorization;


import com.amazonaws.SdkClientException;
import com.google.common.annotations.VisibleForTesting;
import software.amazon.awssdk.services.cloudwatch.model.InvalidParameterValueException;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.AuthorizeEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.AuthorizeEndpointAccessResponse;
import software.amazon.awssdk.services.redshift.model.ClusterNotFoundException;
import software.amazon.awssdk.services.redshift.model.EndpointAuthorizationAlreadyExistsException;
import software.amazon.awssdk.services.redshift.model.EndpointAuthorizationsPerClusterLimitExceededException;
import software.amazon.awssdk.services.redshift.model.InvalidAuthorizationStateException;
import software.amazon.awssdk.services.redshift.model.RedshiftException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;


public class CreateHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<RedshiftClient> proxyClient,
        final Logger logger) {

        this.logger = logger;

        ResourceModel resourceModel = request.getDesiredResourceState();
        parseResourceModel(resourceModel);

        return ProgressEvent.progress(resourceModel, callbackContext)
                .then(progress ->
                        proxy.initiate("AWS-Redshift-EndpointAuthorization::Create",
                                proxyClient,
                                progress.getResourceModel(),
                                progress.getCallbackContext())
                                .translateToServiceRequest(Translator::translateToCreateRequest)
                                .makeServiceCall(this::createEndpointAuthorization)
                                .progress())
        .then(progress ->
                new ReadHandler().handleRequest(proxy, request, progress.getCallbackContext(), proxyClient, logger)
        );
    }

    @VisibleForTesting
    AuthorizeEndpointAccessResponse createEndpointAuthorization(
            final AuthorizeEndpointAccessRequest request,
            final ProxyClient<RedshiftClient> proxyClient) {
        AuthorizeEndpointAccessResponse response = null;

        // Validate that the account is not null
        if (!Validator.doesExist(request.account())) {
            throw new CfnInvalidRequestException(request.toString());
        }

        try {
            // Validate the auth doesn't exist. If it does, this throws the CfnAlreadyExistsException
            Validator.validateAuthNotExists(request, proxyClient);

            logAPICall(request, "AuthorizeEndpointAccess", logger);

            response = proxyClient.injectCredentialsAndInvokeV2(
                    request, proxyClient.client()::authorizeEndpointAccess
            );
        } catch (EndpointAuthorizationAlreadyExistsException e) {
            throw new CfnAlreadyExistsException(e);
        } catch (ClusterNotFoundException | InvalidParameterValueException | UnsupportedOperationException e) {
            throw new CfnInvalidRequestException(e);
        } catch (InvalidAuthorizationStateException e) {
            throw new CfnResourceConflictException(e);
        } catch (EndpointAuthorizationsPerClusterLimitExceededException e) {
            throw new CfnServiceLimitExceededException(e);
        } catch (RedshiftException | SdkClientException e) {
            throw new CfnGeneralServiceException(e);
        }

        return response;
    }

    void parseResourceModel(ResourceModel resourceModel) {
        // If the Create handler receives a model that does not contain the account parameter,
        // we should derive the account from the grantee parameter.
        if (!Validator.doesExist(resourceModel.getAccount())) {
            resourceModel.setAccount(resourceModel.getGrantee());
        }
    }
}
