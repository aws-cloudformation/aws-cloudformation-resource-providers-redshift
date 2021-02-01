package software.amazon.redshift.clustersecuritygroup;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterSecurityGroupNotFoundException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.HandlerErrorCode;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

public class DeleteHandler extends BaseHandlerStd {
    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
            final AmazonWebServicesClientProxy proxy,
            final ResourceHandlerRequest<ResourceModel> request,
            final CallbackContext callbackContext,
            final ProxyClient<RedshiftClient> proxyClient,
            final Logger logger) {
        return proxy.initiate("AWS-Redshift-ClusterSecurityGroup::Delete", proxyClient, request.getDesiredResourceState(), callbackContext)
                .translateToServiceRequest(Translator::translateToDeleteRequest)
                .makeServiceCall((deleteSecurityGroupRequest, proxyInvocation) -> proxyInvocation.injectCredentialsAndInvokeV2(deleteSecurityGroupRequest, proxyInvocation.client()::deleteClusterSecurityGroup))
                .handleError((deleteSecurityGroupRequest, exception, client, resourceModel, cxt) -> {
                    if (exception instanceof ClusterSecurityGroupNotFoundException)
                        return ProgressEvent.defaultFailureHandler(exception, HandlerErrorCode.NotFound);
                    throw exception;
                })
                .done((deleteSecurityGroupRequest, deleteDbSubnetGroupResponse, client, model, cxt) -> ProgressEvent.defaultSuccessHandler(null));
    }
}
