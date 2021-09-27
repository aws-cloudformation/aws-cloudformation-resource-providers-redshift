package software.amazon.redshift.endpointauthorization;

import com.google.common.annotations.VisibleForTesting;
import software.amazon.awssdk.services.cloudwatch.model.InvalidParameterValueException;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.AuthorizeEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.AuthorizeEndpointAccessResponse;
import software.amazon.awssdk.services.redshift.model.ClusterNotFoundException;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAuthorizationRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAuthorizationResponse;
import software.amazon.awssdk.services.redshift.model.EndpointAuthorizationAlreadyExistsException;
import software.amazon.awssdk.services.redshift.model.EndpointAuthorizationNotFoundException;
import software.amazon.awssdk.services.redshift.model.EndpointAuthorizationsPerClusterLimitExceededException;
import software.amazon.awssdk.services.redshift.model.InvalidAuthorizationStateException;
import software.amazon.awssdk.services.redshift.model.InvalidEndpointStateException;
import software.amazon.awssdk.services.redshift.model.RevokeEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.RevokeEndpointAccessResponse;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.OperationStatus;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Collections;

import static software.amazon.redshift.endpointauthorization.Validator.validateReadReturnedAuthorization;

/**
 * If the update request has a list of VPCs...
 * <ul>
 *     <li>Get the existing auth
 *     <li>Determine existing VPCs
 *     <li>If there are any existing ones that are in the update request, ignore them
 *     <li>If there are any that do not exist in the update request, delete those.
 *     <ul>
 *         <li>Use the current effective force setting to do the deletion.</li>
 *     </ul>
 *     <li>Add in the VPCs that are in the VPC id list
 * </ul>
 * If the update request has no vpc ids, then we authorize all like normal.
 */
public class UpdateHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<RedshiftClient> proxyClient,
            final Logger logger) {

        this.logger = logger;
        boolean force = Boolean.TRUE.equals(request.getPreviousResourceState().getForce());
        ResourceModel desiredResourceModel = request.getDesiredResourceState().toBuilder()
                .vpcIds(request.getDesiredResourceState().getVpcIds() == null ?
                        Collections.emptyList() : request.getDesiredResourceState().getVpcIds())
                .allowedAllVPCs(request.getDesiredResourceState().getVpcIds() == null ||
                        request.getDesiredResourceState().getVpcIds().isEmpty())
                .build();

        return ProgressEvent.progress(desiredResourceModel, callbackContext)
                .then(progress ->
                        proxy.initiate("AWS-Redshift-EndpointAuthorization::Update::ReadOldResource",
                                        proxyClient,
                                        progress.getResourceModel(),
                                        progress.getCallbackContext())
                                .translateToServiceRequest(Translator::translateToReadRequest)
                                .makeServiceCall(this::readEndpointAuthorization)
                                .handleError(this::readEndpointAuthorizationErrorHandler)
                                .done((readRequest, readResponse, client, model, context) -> ProgressEvent.<ResourceModel, CallbackContext>builder()
                                        .callbackContext(callbackContext)
                                        .callbackDelaySeconds(0)
                                        .resourceModel(Translator.translateFromReadResponse(readResponse))
                                        .status(OperationStatus.IN_PROGRESS)
                                        .build()))

                .then(progress ->
                        proxy.initiate("AWS-Redshift-EndpointAuthorization::Update::DeleteOldResource",
                                        proxyClient,
                                        progress.getResourceModel(),
                                        progress.getCallbackContext())
                                .translateToServiceRequest(currentResourceModel -> Translator.translateToUpdateRevokeRequest(
                                        desiredResourceModel, currentResourceModel, force))
                                .makeServiceCall(this::revokeEndpointAuthorization)
                                .handleError(this::revokeEndpointAuthorizationErrorHandler)
                                .progress())

                .then(progress ->
                        proxy.initiate("AWS-Redshift-EndpointAuthorization::Update::CreateNewResource",
                                        proxyClient,
                                        progress.getResourceModel(),
                                        progress.getCallbackContext())
                                .translateToServiceRequest(currentResourceModel -> Translator.translateToUpdateAuthorizeRequest(
                                        desiredResourceModel, currentResourceModel))
                                .makeServiceCall(this::authorizeEndpointAccess)
                                .handleError(this::authorizeEndpointAccessErrorHandler)
                                .progress())

                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    @VisibleForTesting
    DescribeEndpointAuthorizationResponse readEndpointAuthorization(final DescribeEndpointAuthorizationRequest request,
                                                                    final ProxyClient<RedshiftClient> proxyClient) {
        DescribeEndpointAuthorizationResponse response;
        response = proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::describeEndpointAuthorization);

        logAPICall(request, "DescribeEndpointAuthorization", logger);
        validateReadReturnedAuthorization(request, response);
        return response;
    }

    private ProgressEvent<ResourceModel, CallbackContext> readEndpointAuthorizationErrorHandler(final DescribeEndpointAuthorizationRequest awsRequest,
                                                                                                final Exception exception,
                                                                                                final ProxyClient<RedshiftClient> client,
                                                                                                final ResourceModel model,
                                                                                                final CallbackContext context) {
        if (exception instanceof ClusterNotFoundException ||
                exception instanceof CfnNotFoundException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.NotFound);
        } else {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.GeneralServiceException);
        }
    }

    @VisibleForTesting
    RevokeEndpointAccessResponse revokeEndpointAuthorization(final RevokeEndpointAccessRequest awsRequest,
                                                             final ProxyClient<RedshiftClient> proxyClient) {
        // If acquire an empty request, do nothing.
        if (awsRequest.equals(RevokeEndpointAccessRequest.builder().build())) {
            logAPICall(awsRequest, "RevokeEndpointAccess-NoOperate", logger);
            return RevokeEndpointAccessResponse.builder().build();

        } else {
            logAPICall(awsRequest, "RevokeEndpointAccess", logger);
            return proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::revokeEndpointAccess);
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> revokeEndpointAuthorizationErrorHandler(final RevokeEndpointAccessRequest awsRequest,
                                                                                                  final Exception exception,
                                                                                                  final ProxyClient<RedshiftClient> client,
                                                                                                  final ResourceModel model,
                                                                                                  final CallbackContext context) {
        if (exception instanceof EndpointAuthorizationNotFoundException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.NotFound);

        } else if (exception instanceof ClusterNotFoundException ||
                exception instanceof InvalidAuthorizationStateException ||
                exception instanceof InvalidEndpointStateException ||
                exception instanceof InvalidParameterValueException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.InvalidRequest);

        } else {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.GeneralServiceException);
        }
    }

    @VisibleForTesting
    AuthorizeEndpointAccessResponse authorizeEndpointAccess(final AuthorizeEndpointAccessRequest awsRequest,
                                                            final ProxyClient<RedshiftClient> proxyClient) {
        // If acquire an empty request, do nothing.
        if (awsRequest.equals(AuthorizeEndpointAccessRequest.builder().build())) {
            logAPICall(awsRequest, "AuthorizeEndpointAccess-NoOperate", logger);
            return AuthorizeEndpointAccessResponse.builder().build();

        } else {
            logAPICall(awsRequest, "AuthorizeEndpointAccess", logger);
            return proxyClient.injectCredentialsAndInvokeV2(awsRequest, proxyClient.client()::authorizeEndpointAccess);
        }
    }

    private ProgressEvent<ResourceModel, CallbackContext> authorizeEndpointAccessErrorHandler(final AuthorizeEndpointAccessRequest awsRequest,
                                                                                              final Exception exception,
                                                                                              final ProxyClient<RedshiftClient> client,
                                                                                              final ResourceModel model,
                                                                                              final CallbackContext context) {
        if (exception instanceof EndpointAuthorizationAlreadyExistsException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.AlreadyExists);

        } else if (exception instanceof ClusterNotFoundException ||
                exception instanceof UnsupportedOperationException ||
                exception instanceof InvalidParameterValueException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.InvalidRequest);

        } else if (exception instanceof InvalidAuthorizationStateException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.ResourceConflict);

        } else if (exception instanceof EndpointAuthorizationsPerClusterLimitExceededException) {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.ServiceLimitExceeded);

        } else {
            return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.GeneralServiceException);
        }
    }
}
