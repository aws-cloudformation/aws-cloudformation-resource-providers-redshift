package software.amazon.redshift.cluster;

import com.amazonaws.util.StringUtils;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.ClusterAlreadyExistsException;
import software.amazon.awssdk.services.redshift.model.CreateClusterRequest;
import software.amazon.awssdk.services.redshift.model.CreateClusterResponse;
import software.amazon.awssdk.services.redshift.model.DescribeClustersRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClustersResponse;
import software.amazon.awssdk.services.redshift.model.InvalidClusterStateException;
import software.amazon.awssdk.services.redshift.model.InvalidRetentionPeriodException;
import software.amazon.awssdk.services.redshift.model.RedshiftException;
import software.amazon.cloudformation.exceptions.CfnAlreadyExistsException;
import software.amazon.cloudformation.exceptions.CfnGeneralServiceException;
import software.amazon.cloudformation.exceptions.CfnInvalidRequestException;
import software.amazon.cloudformation.exceptions.CfnServiceLimitExceededException;
import software.amazon.cloudformation.proxy.AmazonWebServicesClientProxy;
import software.amazon.cloudformation.proxy.Logger;
import software.amazon.cloudformation.proxy.ProgressEvent;
import software.amazon.cloudformation.proxy.ProxyClient;
import software.amazon.cloudformation.proxy.ResourceHandlerRequest;
import software.amazon.cloudformation.resource.IdentifierUtils;

import java.util.UUID;


public class CreateHandler extends BaseHandlerStd {
    private Logger logger;
    private static final int MAX_CLUSTER_IDENTIFIER_LENGTH = 63;

    protected ProgressEvent<ResourceModel, CallbackContext> handleRequest(
        final AmazonWebServicesClientProxy proxy,
        final ResourceHandlerRequest<ResourceModel> request,
        final CallbackContext callbackContext,
        final ProxyClient<RedshiftClient> proxyClient,
        final Logger logger) {

        this.logger = logger;

        prepareResourceModel(request);
        final ResourceModel resourceModel = request.getDesiredResourceState();

        return ProgressEvent.progress(resourceModel, callbackContext)
                .then(progress -> proxy.initiate("AWS-Redshift-Cluster::Create", proxyClient, resourceModel, callbackContext)
                        .translateToServiceRequest((m) -> Translator.translateToCreateRequest(resourceModel))
                        .makeServiceCall(this::createClusterResource)
                        .stabilize((_request, _response, _client, _model, _context) -> isClusterActive(_client, _model, _context))
                        .progress())
                .then(progress -> new ReadHandler().handleRequest(proxy, request, callbackContext, proxyClient, logger));
    }

    private CreateClusterResponse createClusterResource(
            final CreateClusterRequest createRequest,
            final ProxyClient<RedshiftClient> proxyClient) {
        CreateClusterResponse createResponse = null;

        try {
            createResponse = proxyClient.injectCredentialsAndInvokeV2(createRequest, proxyClient.client()::createCluster);
        } catch (final ClusterAlreadyExistsException e) {
            throw new CfnAlreadyExistsException(ResourceModel.TYPE_NAME, createRequest.clusterIdentifier());
        }  catch (final InvalidClusterStateException | InvalidRetentionPeriodException e) {
            throw new CfnInvalidRequestException(createRequest.toString(), e);
        } catch (SdkClientException | RedshiftException e) {
            throw new CfnGeneralServiceException(createRequest.toString(), e);
        }
        logger.log(String.format("%s successfully created.", ResourceModel.TYPE_NAME));

        return createResponse;
    }

    private void prepareResourceModel(ResourceHandlerRequest<ResourceModel> request) {
        if (request.getDesiredResourceState() == null) {
            request.setDesiredResourceState(new ResourceModel());
        }
        final ResourceModel model = request.getDesiredResourceState();

        String logicalResourceIdentifier = StringUtils.isNullOrEmpty(request.getLogicalResourceIdentifier())
                ? "cluster-" + UUID.randomUUID().toString() : request.getLogicalResourceIdentifier();

        if (StringUtils.isNullOrEmpty(model.getClusterIdentifier())) {
            model.setClusterIdentifier(
                    IdentifierUtils.generateResourceIdentifier(
                            logicalResourceIdentifier,
                            request.getClientRequestToken(),
                            MAX_CLUSTER_IDENTIFIER_LENGTH
                    ).toLowerCase()
            );
        }
    }
}
