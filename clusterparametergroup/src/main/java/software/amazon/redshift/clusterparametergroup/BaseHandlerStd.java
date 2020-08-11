package software.amazon.redshift.clusterparametergroup;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.DescribeClusterParametersResponse;
import software.amazon.awssdk.services.redshift.model.ModifyClusterParameterGroupResponse;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;

import java.util.Set;
import java.util.stream.Collectors;

// Placeholder for the functionality that could be shared across Create/Read/Update/Delete/List Handlers

public abstract class BaseHandlerStd extends BaseHandler<CallbackContext> {

  protected static int CALLBACK_DELAY_SECONDS = 5 * 60; // 5 min for propagation
  protected static int NO_CALLBACK_DELAY = 0;

  @Override
  public final ProgressEvent<ResourceModel, CallbackContext> handleRequest(
    final AmazonWebServicesClientProxy proxy,
    final ResourceHandlerRequest<ResourceModel> request,
    final CallbackContext callbackContext,
    final Logger logger) {
    return handleRequest(
      proxy,
      request,
      callbackContext != null ? callbackContext : new CallbackContext(),
      proxy.newProxy(ClientBuilder::getClient),
      logger
    );
  }

  protected abstract ProgressEvent<ResourceModel, CallbackContext> handleRequest(
    final AmazonWebServicesClientProxy proxy,
    final ResourceHandlerRequest<ResourceModel> request,
    final CallbackContext callbackContext,
    final ProxyClient<RedshiftClient> proxyClient,
    final Logger logger);

  protected ProgressEvent<ResourceModel, CallbackContext> applyParameters(final AmazonWebServicesClientProxy proxy,
                                                                          final ProxyClient<RedshiftClient> proxyClient,
                                                                          final ResourceModel model,
                                                                          final CallbackContext callbackContext) {
    System.out.println("applyParameters:: model:: " + model);

    if (callbackContext.isParametersApplied()) {
      System.out.println("applyParameters:: ProgressEvent:: " + ProgressEvent.defaultInProgressHandler(callbackContext, NO_CALLBACK_DELAY, model));
      return ProgressEvent.defaultInProgressHandler(callbackContext, NO_CALLBACK_DELAY, model);
    }

    callbackContext.setParametersApplied(true);

    ProgressEvent<ResourceModel, CallbackContext> progress = ProgressEvent.defaultInProgressHandler(callbackContext, CALLBACK_DELAY_SECONDS, model);

    if (model.getParameters() == null || model.getParameters().isEmpty()) {
      System.out.println("applyParameters:: progress:: " + progress);
      return progress;
    }

    Set<String> paramNames = model.getParameters().stream()
            .map(parameter -> parameter.getParameterName()).collect(Collectors.toSet());

    String marker = null;
    do {
      final DescribeClusterParametersResponse describeClusterParametersResponse = proxyClient.injectCredentialsAndInvokeV2(
              Translator.describeClusterParametersRequest(model, marker), proxyClient.client()::describeClusterParameters);

      marker = describeClusterParametersResponse.marker();

      final Set<software.amazon.awssdk.services.redshift.model.Parameter> params =
              Translator.getParametersToModify(model, describeClusterParametersResponse.parameters());

      // if no params need to be modified then skip the api invocation
      if (params.isEmpty()) continue;

      // substract set of found and modified params
      paramNames.removeAll(params.stream().map(parameter -> parameter.parameterName()).collect(Collectors.toSet()));

      progress = proxy.initiate("AWS-Redshift-ClusterParameterGroup::Modify::" + marker, proxyClient, model, callbackContext)
              .translateToServiceRequest((resourceModel) -> Translator.modifyDbClusterParameterGroupRequest(resourceModel, params))
              .makeServiceCall((request, proxyInvocation) -> proxyInvocation.injectCredentialsAndInvokeV2(request, proxyInvocation.client()::modifyClusterParameterGroup))
              .progress(CALLBACK_DELAY_SECONDS);
    } while (!StringUtils.isNullOrEmpty(marker));
    // if there are parameters left that couldn't be found in rds api then they are invalid
    if (!paramNames.isEmpty()) throw new CfnInvalidRequestException("Invalid / Unsupported Parameter: " + paramNames.stream().findFirst().get());
    System.out.println("applyParameters:: progress:: " + progress);
    return progress;
  }

}
