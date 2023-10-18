package software.amazon.redshift.resourcepolicy;

import software.amazon.awssdk.services.redshift.model.PutResourcePolicyRequest;
import software.amazon.awssdk.services.redshift.model.GetResourcePolicyRequest;
import software.amazon.awssdk.services.redshift.model.GetResourcePolicyResponse;
import software.amazon.awssdk.services.redshift.model.DeleteResourcePolicyRequest;

/**
 * This class is a centralized placeholder for
 *  - api request construction
 *  - object translation to/from aws sdk
 *  - resource model construction for read/list handlers
 */

public class Translator {

  /**
   * Request to create a resource
   * @param model resource model
   * @return awsRequest the aws service request to create a resource
   */
  static PutResourcePolicyRequest translateToCreateRequest(final ResourceModel model) {
    return PutResourcePolicyRequest.builder()
            .resourceArn(model.getResourceArn())
            .policy(model.getPolicy())
            .build();
  }

  /**
   * Request to read a resource
   * @param model resource model
   * @return awsRequest the aws service request to describe a resource
   */
  static GetResourcePolicyRequest translateToReadRequest(final ResourceModel model) {
    return GetResourcePolicyRequest.builder()
            .resourceArn(model.getResourceArn())
            .build();
  }

  /**
   * Translates resource object from sdk into a resource model
   * @param awsResponse the aws service describe resource response
   * @return model resource model
   */
  static ResourceModel translateFromReadResponse(final GetResourcePolicyResponse getResponse) {
    return ResourceModel.builder()
            .resourceArn(getResponse.resourcePolicy().resourceArn())
            .policy(getResponse.resourcePolicy().policy())
            .build();
  }

  /**
   * Request to delete a resource
   * @param model resource model
   * @return awsRequest the aws service request to delete a resource
   */
  static DeleteResourcePolicyRequest translateToDeleteRequest(final ResourceModel model) {
    return DeleteResourcePolicyRequest.builder()
            .resourceArn(model.getResourceArn())
            .build();
  }
}
