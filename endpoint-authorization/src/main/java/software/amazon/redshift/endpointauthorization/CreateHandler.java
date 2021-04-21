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

        // Resets the callback delay
//        ProgressEvent<ResourceModel, CallbackContext> prog = ProgressEvent.progress(resourceModel, callbackContext);
//
//        try {
//            logger.log("VALIDATING");
//            validateAuthNotExists(Translator.translateToCreateRequest(resourceModel), proxyClient);
//        } catch (Exception e) {
//            logger.log("INSIDE THE CATCH");
//            return ProgressEvent.progress(resourceModel, callbackContext)
//                    .then(progress ->
//                            getReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger)
//                    );
//        }
        // its null for the first invocation, not null otherwise
//        if (callbackContext. == null) {
//            logger.log("NULL CALLBACK CTC");
//        }
//        if (callbackContext != null) {
//            logger.log(callbackContext.toString());
//            return prog.then(progress ->
//                            getReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger)
//                    );
//        }

//        return prog.then(progress ->
//                        proxy.initiate("AWS-Redshift-EndpointAuthorization::Create",
//                                proxyClient,
//                                progress.getResourceModel(),
//                                progress.getCallbackContext())
//                                .translateToServiceRequest(Translator::translateToCreateRequest)
//                                .makeServiceCall(this::createEndpointAuthorization)
//                                .progress(120)); // this pause happens when we return from this handler
        // it will wait 1 minute until it calls the handler again

//         AWS-Redshift- EndpointAuthorization::Create
        return ProgressEvent.progress(resourceModel, callbackContext)
                .then(progress ->
                        proxy.initiate("AWS-Redshift-EndpointAuthorization::Create",
                                proxyClient,
                                progress.getResourceModel(),
                                progress.getCallbackContext())
                                .translateToServiceRequest(Translator::translateToCreateRequest)
                                .makeServiceCall(this::createEndpointAuthorization)
                                .progress()) // since the callback delay is not 0, we do not
                // chain to the next step, and instead return ourselves. This return output is fed
                // into the input of another handler, but since we memoized the call to the service
                // nothing happens?
        .then(progress ->
                getReadHandler().handleRequest(proxy, request, progress.getCallbackContext(), proxyClient, logger)
        );
    }

    @VisibleForTesting
    AuthorizeEndpointAccessResponse createEndpointAuthorization(
            final AuthorizeEndpointAccessRequest request,
            final ProxyClient<RedshiftClient> proxyClient) {
        AuthorizeEndpointAccessResponse response = null;

        // Validate the auth doesn't exist. If it does, this throws the CfnAlreadyExistsException
        validateAuthNotExists(request, proxyClient);

        // Validate that the account is not null
        if (doesNotExist(request.account())) {
            throw new CfnInvalidRequestException(request.toString());
        }

        try {
            response = proxyClient.injectCredentialsAndInvokeV2(
                    request, proxyClient.client()::authorizeEndpointAccess
            );
        } catch (EndpointAuthorizationAlreadyExistsException e) {
            throw new CfnAlreadyExistsException(e);
        } catch (ClusterNotFoundException | InvalidParameterValueException | UnsupportedOperationException e) {
            throw new CfnInvalidRequestException(request.toString(), e);
        } catch (InvalidAuthorizationStateException e) {
            throw new CfnResourceConflictException(e);
        } catch (EndpointAuthorizationsPerClusterLimitExceededException e) {
            throw new CfnServiceLimitExceededException(e);
        } catch (RedshiftException | SdkClientException e) {
            throw new CfnGeneralServiceException(request.toString(), e);
        }

        return response;
    }

    void parseResourceModel(ResourceModel resourceModel) {
        // If the Create handler receives a model that does not contain the account parameter,
        // we should derive the account from the grantee parameter.
        if (doesNotExist(resourceModel.getAccount())) {
            resourceModel.setAccount(resourceModel.getGrantee());
        }
    }

}
