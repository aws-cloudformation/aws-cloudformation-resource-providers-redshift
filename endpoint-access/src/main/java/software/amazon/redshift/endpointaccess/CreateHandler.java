package software.amazon.redshift.endpointaccess;

import com.amazonaws.util.StringUtils;
import lombok.NonNull;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.AccessToClusterDeniedException;
import software.amazon.awssdk.services.redshift.model.ClusterNotFoundException;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetGroupNotFoundException;
import software.amazon.awssdk.services.redshift.model.CreateEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.CreateEndpointAccessResponse;
import software.amazon.awssdk.services.redshift.model.EndpointAlreadyExistsException;
import software.amazon.awssdk.services.redshift.model.EndpointsPerAuthorizationLimitExceededException;
import software.amazon.awssdk.services.redshift.model.EndpointsPerClusterLimitExceededException;
import software.amazon.awssdk.services.redshift.model.UnauthorizedOperationException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import javax.annotation.Nullable;
import java.util.UUID;

import static software.amazon.cloudformation.resource.IdentifierUtils.generateResourceIdentifier;
import static software.amazon.redshift.endpointaccess.EndpointAccessStabilizers.isEndpointActive;


public class CreateHandler extends BaseHandlerStd {
    private Logger logger;
    private static final int MAX_ENDPOINT_NAME_LENGTH = 30;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<RedshiftClient> proxyClient,
        final Logger logger) {

        this.logger = logger;

        ResourceModel resourceModel = prepareResourceModel(request);

        logResourceModelRequest(resourceModel, logger);
        if (hasReadOnlyProperties(resourceModel)) {
            throw new CfnInvalidRequestException("Attempting to set a ReadOnly Property.");
        }

        Validator.validateCreateRequest(resourceModel, logger);

        return ProgressEvent.progress(resourceModel, callbackContext)
                .then(progress -> proxy.initiate(
                        "AWS-Redshift-EndpointAccess::Create",
                        proxyClient,
                        progress.getResourceModel(),
                        progress.getCallbackContext())
                        .translateToServiceRequest(Translator::translateToCreateRequest)
                        .makeServiceCall(this::createEndpointAccess)
                        .stabilize((_req, _resp, client, model, ctx) -> isEndpointActive(client, model, ctx))
                        .progress())
                .then(progress ->
                        new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger)
                );
    }

    private CreateEndpointAccessResponse createEndpointAccess(
            @NonNull final CreateEndpointAccessRequest createRequest,
            @NonNull final ProxyClient<RedshiftClient> proxyClient) {
        CreateEndpointAccessResponse createResponse = null;

        logAPICall(createRequest, "CreateEndpointAccess", logger);
        try {
            createResponse = proxyClient.injectCredentialsAndInvokeV2(
                    createRequest, proxyClient.client()::createEndpointAccess
            );
        } catch (EndpointAlreadyExistsException e) {
            throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, createRequest.endpointName());
        } catch (ClusterNotFoundException | ClusterSubnetGroupNotFoundException e) {
            throw new CfnInvalidRequestException(createRequest.toString(), e);
        } catch (EndpointsPerClusterLimitExceededException | EndpointsPerAuthorizationLimitExceededException e) {
            throw new CfnServiceLimitExceededException(ResourceModel.TYPE_NAME, e.toString());
        } catch (AccessToClusterDeniedException | UnauthorizedOperationException e) {
            throw new CfnAccessDeniedException(e);
        } catch (Exception e) {
            // InvalidClusterSecurityGroupStateFault, InvalidClusterStateFault, UnsupportedOperationFault
            throw new CfnInvalidRequestException(ResourceModel.TYPE_NAME, e);
        }

        return createResponse;
    }

    private ResourceModel prepareResourceModel(@NonNull ResourceHandlerRequest<ResourceModel> request) {
        if (request.getDesiredResourceState() == null) {
            request.setDesiredResourceState(new ResourceModel());
        }

        final ResourceModel model = request.getDesiredResourceState();

        if (StringUtils.isNullOrEmpty(model.getEndpointName())) {
            String logicalResourceIdentifier = getLogicalResourceIdentifier(request.getLogicalResourceIdentifier());

            String endpointName = buildEndpointName(logicalResourceIdentifier, request.getClientRequestToken());
            model.setEndpointName(endpointName);
        }

        return model;
    }

    private String buildEndpointName(@NonNull String logicalResourceIdentifier, @NonNull String clientRequestToken) {
        return generateResourceIdentifier(logicalResourceIdentifier, clientRequestToken, MAX_ENDPOINT_NAME_LENGTH)
                .toLowerCase();
    }

    private String getLogicalResourceIdentifier(@Nullable String logicalResourceIdentifier) {
        return StringUtils.isNullOrEmpty(logicalResourceIdentifier)
                ? "endpoint-" + UUID.randomUUID().toString()
                : logicalResourceIdentifier;
    }
}
