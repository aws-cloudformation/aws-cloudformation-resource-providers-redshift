package software.amazon.redshift.endpointaccess;

import lombok.NonNull;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterNotFoundException;
import software.amazon.awssdk.services.redshift.model.CreateEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.CreateEndpointAccessResponse;
import software.amazon.awssdk.services.redshift.model.DeleteEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.DeleteEndpointAccessResponse;
import software.amazon.awssdk.services.redshift.model.EndpointAlreadyExistsException;
import software.amazon.awssdk.services.redshift.model.EndpointNotFoundException;
import software.amazon.awssdk.services.redshift.model.InvalidEndpointStateException;
import software.amazon.awssdk.services.redshift.model.UnauthorizedOperationException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import static software.amazon.redshift.endpointaccess.EndpointAccessStabilizers.isEndpointActive;
import static software.amazon.redshift.endpointaccess.EndpointAccessStabilizers.isEndpointDeleted;

public class UpdateHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<RedshiftClient> proxyClient,
            final Logger logger) {

        this.logger = logger;

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress -> proxy.initiate("AWS-Redshift-EndpointAccess::Update::DeleteOldResource",
                                proxyClient,
                                progress.getResourceModel(),
                                progress.getCallbackContext())
                        .translateToServiceRequest(Translator::translateToDeleteRequest)
                        .makeServiceCall(this::deleteEndpointAccess)
                        .stabilize((awsRequest, response, client, model, ctx) -> isEndpointDeleted(client, model, ctx))
                        .handleError(this::deleteEndpointAccessErrorHandler)
                        .progress())

                .then(progress -> proxy.initiate("AWS-Redshift-EndpointAccess::Update::CreateNewResource",
                                proxyClient,
                                progress.getResourceModel(),
                                progress.getCallbackContext())
                        .translateToServiceRequest(Translator::translateToCreateRequest)
                        .makeServiceCall(this::createEndpointAccess)
                        .stabilize((awsRequest, response, client, model, ctx) -> isEndpointActive(client, model, ctx))
                        .handleError(this::createEndpointAccessErrorHandler)
                        .progress())

                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private DeleteEndpointAccessResponse deleteEndpointAccess(@NonNull final DeleteEndpointAccessRequest awsRequest,
                                                              @NonNull final ProxyClient<RedshiftClient> proxyClient) {

        DeleteEndpointAccessResponse response;
        response = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::deleteEndpointAccess);

        logAPICall(awsRequest, "DeleteEndpointAccess", logger);
        return response;
    }

    private ProgressEvent<ResourceModel, CallbackContext> deleteEndpointAccessErrorHandler(final DeleteEndpointAccessRequest awsRequest,
                                                                                           final Exception exception,
                                                                                           final ProxyClient<RedshiftClient> client,
                                                                                           final ResourceModel model,
                                                                                           final CallbackContext context) {
        if (exception instanceof EndpointNotFoundException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.NotFound);

        } else if (exception instanceof InvalidEndpointStateException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.ResourceConflict);

        } else if (exception instanceof ClusterNotFoundException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.InvalidRequest);

        } else {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.GeneralServiceException);
        }
    }

    private CreateEndpointAccessResponse createEndpointAccess(@NonNull final CreateEndpointAccessRequest awsRequest,
                                                              @NonNull final ProxyClient<RedshiftClient> proxyClient) {
        CreateEndpointAccessResponse response;
        response = proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::createEndpointAccess);

        logAPICall(awsRequest, "CreateEndpointAccess", logger);
        return response;
    }

    private ProgressEvent<ResourceModel, CallbackContext> createEndpointAccessErrorHandler(final CreateEndpointAccessRequest awsRequest,
                                                                                           final Exception exception,
                                                                                           final ProxyClient<RedshiftClient> client,
                                                                                           final ResourceModel model,
                                                                                           final CallbackContext context) {
        if (exception instanceof EndpointAlreadyExistsException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.AlreadyExists);

        } else if (exception instanceof InvalidEndpointStateException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.ResourceConflict);

        } else if (exception instanceof ClusterNotFoundException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.InvalidRequest);

        } else if (exception instanceof UnauthorizedOperationException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.AccessDenied);

        } else {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.GeneralServiceException);
        }
    }
}
