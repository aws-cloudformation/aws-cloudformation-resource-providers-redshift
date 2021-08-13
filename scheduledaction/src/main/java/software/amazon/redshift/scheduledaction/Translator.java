package software.amazon.redshift.scheduledaction;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import software.amazon.awssdk.services.redshift.model.CreateScheduledActionRequest;
import software.amazon.awssdk.services.redshift.model.DeleteScheduledActionRequest;
import software.amazon.awssdk.services.redshift.model.DescribeScheduledActionsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeScheduledActionsResponse;
import software.amazon.awssdk.services.redshift.model.ModifyScheduledActionRequest;
import software.amazon.awssdk.services.redshift.model.ScheduledActionType;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This class is a centralized placeholder for
 * - api request construction
 * - object translation to/from aws sdk
 * - resource model construction for read/list handlers
 */

public class Translator {
    private static final Gson GSON = new GsonBuilder().create();

    /**
     * Request to create a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to create a resource
     */
    static CreateScheduledActionRequest translateToCreateRequest(final ResourceModel model) {
        return CreateScheduledActionRequest.builder()
                .scheduledActionName(model.getScheduledActionName())
                .targetAction(translateToSdkTargetAction(model.getTargetAction()))
                .schedule(model.getSchedule())
                .iamRole(model.getIamRole())
                .scheduledActionDescription(model.getScheduledActionDescription())
                .startTime(model.getStartTime() == null ? null : Instant.parse(model.getStartTime()))
                .endTime(model.getEndTime() == null ? null : Instant.parse(model.getEndTime()))
                .enable(model.getEnable())
                .build();
    }

    /**
     * Request to read a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to describe a resource
     */
    static DescribeScheduledActionsRequest translateToReadRequest(final ResourceModel model) {
        return DescribeScheduledActionsRequest.builder()
                .scheduledActionName(model.getScheduledActionName())
                .build();
    }

    /**
     * Translates resource object from sdk into a resource model
     *
     * @param awsResponse the aws service describe resource response
     * @return model resource model
     */
    static ResourceModel translateFromReadResponse(final DescribeScheduledActionsResponse awsResponse) {
        return translateFromListResponse(awsResponse)
                .stream()
                .findAny()
                .orElse(ResourceModel.builder().build());
    }

    /**
     * Request to delete a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to delete a resource
     */
    static DeleteScheduledActionRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteScheduledActionRequest.builder()
                .scheduledActionName(model.getScheduledActionName())
                .build();
    }

    /**
     * Request to update properties of a previously created resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to modify a resource
     */
    static ModifyScheduledActionRequest translateToUpdateRequest(final ResourceModel model) {
        return ModifyScheduledActionRequest.builder()
                .scheduledActionName(model.getScheduledActionName())
                .targetAction(translateToSdkTargetAction(model.getTargetAction()))
                .schedule(model.getSchedule())
                .iamRole(model.getIamRole())
                .scheduledActionDescription(model.getScheduledActionDescription())
                .startTime(model.getStartTime() == null ? null : Instant.parse(model.getStartTime()))
                .endTime(model.getEndTime() == null ? null : Instant.parse(model.getEndTime()))
                .enable(model.getEnable())
                .build();
    }

    /**
     * Request to list resources
     *
     * @param nextToken token passed to the aws service list resources request
     * @return awsRequest the aws service request to list resources within aws account
     */
    static DescribeScheduledActionsRequest translateToListRequest(final String nextToken) {
        return DescribeScheduledActionsRequest.builder()
                .marker(nextToken)
                .build();
    }

    /**
     * Translates resource objects from sdk into a resource model (primary identifier only)
     *
     * @param awsResponse the aws service describe resource response
     * @return list of resource models
     */
    static List<ResourceModel> translateFromListResponse(final DescribeScheduledActionsResponse awsResponse) {
        return awsResponse.scheduledActions()
                .stream()
                .map(scheduledAction -> ResourceModel.builder()
                        .scheduledActionName(scheduledAction.scheduledActionName())
                        .targetAction(translateToModelTargetAction(scheduledAction.targetAction()))
                        .schedule(scheduledAction.schedule())
                        .iamRole(scheduledAction.iamRole())
                        .scheduledActionDescription(scheduledAction.scheduledActionDescription())
                        .startTime(scheduledAction.startTime() == null ? null : scheduledAction.startTime().toString())
                        .endTime(scheduledAction.endTime() == null ? null : scheduledAction.endTime().toString())
                        .state(scheduledAction.state() == null ? null : scheduledAction.state().toString())
                        .nextInvocations(scheduledAction.nextInvocations()
                                .stream()
                                .map(Instant::toString)
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());
    }

    private static ScheduledActionType translateToSdkTargetAction(software.amazon.redshift.scheduledaction.ScheduledActionType targetAction) {
        return GSON.fromJson(GSON.toJson(targetAction), ScheduledActionType.class);
    }

    private static software.amazon.redshift.scheduledaction.ScheduledActionType translateToModelTargetAction(ScheduledActionType targetAction) {
        return GSON.fromJson(GSON.toJson(targetAction), software.amazon.redshift.scheduledaction.ScheduledActionType.class);
    }
}
