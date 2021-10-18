package software.amazon.redshift.endpointaccess;

import com.google.common.collect.ImmutableSet;
import lombok.NonNull;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterNotFoundException;
import software.amazon.awssdk.services.redshift.model.DescribeEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.EndpointNotFoundException;
import software.amazon.awssdk.services.redshift.model.InvalidEndpointStateException;
import software.amazon.awssdk.services.redshift.model.ModifyEndpointAccessRequest;
import software.amazon.awssdk.services.redshift.model.ModifyEndpointAccessResponse;
import software.amazon.awssdk.services.redshift.model.UnauthorizedOperationException;
import software.amazon.cloudformation.exceptions.CfnAccessDeniedException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnNotFoundException;
import software.amazon.cloudformation.exceptions.CfnResourceConflictException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Set;

import static software.amazon.redshift.endpointaccess.EndpointAccessStabilizers.isEndpointActive;

public class UpdateHandler extends BaseHandlerStd {
    private Logger logger;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<RedshiftClient> proxyClient,
            final Logger logger) {

        this.logger = logger;

        ResourceModel resourceModel = request.getDesiredResourceState();

        Validator.validateUpdateRequest(proxyClient, resourceModel, logger);

        return ProgressEvent.progress(request.getDesiredResourceState(), callbackContext)
                .then(progress -> proxy.initiate(
                                "AWS-Redshift-EndpointAccess::Update",
                                proxyClient,
                                progress.getResourceModel(),
                                progress.getCallbackContext())
                        .translateToServiceRequest(Translator::translateToUpdateRequest)
                        .makeServiceCall(this::modifyEndpointAccess)
                        .stabilize((awsRequest, response, client, model, ctx) -> isEndpointActive(client, model, ctx))
                        .progress())
                .then(progress -> new ReadHandler()
                        .handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private ModifyEndpointAccessResponse modifyEndpointAccess(
            @NonNull final ModifyEndpointAccessRequest request,
            @NonNull final ProxyClient<RedshiftClient> proxyClient) {
        logAPICall(request, "ModifyEndpointAccess", logger);

        try {
            return proxyClient.injectCredentialsAndInvokeV2(request, proxyClient.client()::modifyEndpointAccess);
        } catch (EndpointNotFoundException e) {
            throw new CfnNotFoundException(ResourceModel.TYPE_NAME, request.endpointName(), e);
        } catch (ClusterNotFoundException e) {
            throw new CfnInvalidRequestException(e);
        } catch (InvalidEndpointStateException e) {
            throw new CfnResourceConflictException(
                    ResourceModel.TYPE_NAME,
                    request.endpointName(),
                    "Endpoint status must be active",
                    e
            );
        } catch (UnauthorizedOperationException e) {
            throw new CfnAccessDeniedException(e);
        } catch (Exception e) {
            // If no VPC security group ID changed, do nothing and return the current endpoint setting.
            Set<String> ignoredExceptionRegexList = ImmutableSet.<String>builder()
                    .add(".*The specified VPC security group identifiers are already associated with the endpoint.*")
                    .build();

            if (ignoredExceptionRegexList.stream().anyMatch(e.getMessage()::matches)) {
                return Translator.translateToUpdateResponse(proxyClient.injectCredentialsAndInvokeV2(
                        DescribeEndpointAccessRequest.builder()
                                .endpointName(request.endpointName())
                                .build(),
                        proxyClient.client()::describeEndpointAccess));

            } else {
                // InvalidClusterStateFault, InvalidClusterSecurityGroupStateFault
                throw new CfnGeneralServiceException(e);
            }
        }
    }
}
