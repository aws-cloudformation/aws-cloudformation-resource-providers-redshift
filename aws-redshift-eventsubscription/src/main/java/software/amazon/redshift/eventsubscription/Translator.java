package software.amazon.redshift.eventsubscription;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import software.amazon.awssdk.services.redshift.model.CreateEventSubscriptionRequest;
import software.amazon.awssdk.services.redshift.model.CreateTagsRequest;
import software.amazon.awssdk.services.redshift.model.DeleteEventSubscriptionRequest;
import software.amazon.awssdk.services.redshift.model.DeleteTagsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEventSubscriptionsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeEventSubscriptionsResponse;
import software.amazon.awssdk.services.redshift.model.DescribeTagsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeTagsResponse;
import software.amazon.awssdk.services.redshift.model.ModifyEventSubscriptionRequest;
import software.amazon.awssdk.services.redshift.model.TaggedResource;

import java.util.Collections;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
     * @param tags  resource+stack tags
     * @return awsRequest the aws service request to create a resource
     */
    static CreateEventSubscriptionRequest translateToCreateRequest(final ResourceModel model, final Map<String, String> tags) {
        return CreateEventSubscriptionRequest.builder()
                .subscriptionName(model.getSubscriptionName())
                .snsTopicArn(model.getSnsTopicArn())
                .sourceType(model.getSourceType())
                .sourceIds(model.getSourceIds())
                .eventCategories(model.getEventCategories())
                .severity(model.getSeverity())
                .enabled(model.getEnabled())
                .tags(translateToSdkTags(translateTagsMapToTagCollection(tags)))
                .build();
    }

    /**
     * Request to read a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to describe a resource
     */
    static DescribeEventSubscriptionsRequest translateToReadRequest(final ResourceModel model) {
        return DescribeEventSubscriptionsRequest.builder()
                .subscriptionName(model.getSubscriptionName())
                .build();
    }

    /**
     * Translates resource object from sdk into a resource model
     *
     * @param awsResponse the aws service describe resource response
     * @return model resource model
     */
    static ResourceModel translateFromReadResponse(final DescribeEventSubscriptionsResponse awsResponse) {
        return awsResponse.eventSubscriptionsList()
                .stream()
                .map(eventSubscription -> ResourceModel.builder()
                        .subscriptionName(eventSubscription.custSubscriptionId())
                        .snsTopicArn(eventSubscription.snsTopicArn())
                        .sourceType(eventSubscription.sourceType())
                        .sourceIds(eventSubscription.sourceIdsList())
                        .eventCategories(new HashSet<>(eventSubscription.eventCategoriesList()))
                        .severity(eventSubscription.severity())
                        .enabled(eventSubscription.enabled())
                        .customerAwsId(eventSubscription.customerAwsId())
                        .custSubscriptionId(eventSubscription.custSubscriptionId())
                        .status(eventSubscription.status())
                        .subscriptionCreationTime(Objects.toString(eventSubscription.subscriptionCreationTime(), null))
                        .sourceIdsList(eventSubscription.sourceIdsList())
                        .eventCategoriesList(new HashSet<>(eventSubscription.eventCategoriesList()))
                        .build())
                .findAny()
                .orElse(ResourceModel.builder().build());
    }

    /**
     * Request to delete a resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to delete a resource
     */
    static DeleteEventSubscriptionRequest translateToDeleteRequest(final ResourceModel model) {
        return DeleteEventSubscriptionRequest.builder()
                .subscriptionName(model.getSubscriptionName())
                .build();
    }

    /**
     * Request to update properties of a previously created resource
     *
     * @param model resource model
     * @return awsRequest the aws service request to modify a resource
     */
    static ModifyEventSubscriptionRequest translateToUpdateRequest(final ResourceModel model) {
        return ModifyEventSubscriptionRequest.builder()
                .subscriptionName(model.getSubscriptionName())
                .snsTopicArn(model.getSnsTopicArn())
                .sourceType(model.getSourceType())
                .sourceIds(model.getSourceIds())
                .eventCategories(model.getEventCategories())
                .severity(model.getSeverity())
                .enabled(model.getEnabled())
                .build();
    }

    /**
     * Request to list resources
     *
     * @param nextToken token passed to the aws service list resources request
     * @return awsRequest the aws service request to list resources within aws account
     */
    static DescribeEventSubscriptionsRequest translateToListRequest(final String nextToken) {
        return DescribeEventSubscriptionsRequest.builder()
                .marker(nextToken)
                .build();
    }

    /**
     * Translates resource objects from sdk into a resource model (primary identifier only)
     *
     * @param awsResponse the aws service describe resource response
     * @return list of resource models
     */
    static List<ResourceModel> translateFromListRequest(final DescribeEventSubscriptionsResponse awsResponse) {
        return awsResponse.eventSubscriptionsList()
                .stream()
                .map(eventSubscription -> ResourceModel.builder()
                        .subscriptionName(eventSubscription.custSubscriptionId())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Request to read tags for a resource
     *
     * @param resourceName the arn of the requested resource
     * @return awsRequest the aws service request to update tags of a resource
     */
    static DescribeTagsRequest translateToReadTagsRequest(final String resourceName) {
        return DescribeTagsRequest.builder()
                .resourceName(resourceName)
                .build();
    }

    /**
     * Translates resource object from sdk into a resource model
     *
     * @param awsResponse the aws service describe resource response
     * @return awsRequest the aws service request to update tags of a resource
     */
    static ResourceModel translateFromReadTagsResponse(final DescribeTagsResponse awsResponse) {
        return ResourceModel.builder()
                .tags(translateToModelTags(awsResponse.taggedResources()
                        .stream()
                        .map(TaggedResource::tag)
                        .collect(Collectors.toList())))
                .build();
    }

    /**
     * Request to update tags for a resource
     *
     * @param desiredResourceState the resource model request to update tags
     * @param currentResourceState the resource model request to delete tags
     * @param resourceName         the arn of the requested resource
     * @return awsRequest the aws service request to update tags of a resource
     */
    static ModifyTagsRequest translateToUpdateTagsRequest(List<Tag> desiredTags,
                                                          List<Tag> currentTags,
                                                          final String resourceName) {
        List<Tag> toBeCreatedTags = subtract(desiredTags, currentTags);
        List<Tag> toBeDeletedTags = subtract(currentTags, desiredTags);

        return ModifyTagsRequest.builder()
                .createNewTagsRequest(CreateTagsRequest.builder()
                        .tags(translateToSdkTags(toBeCreatedTags))
                        .resourceName(resourceName)
                        .build())
                .deleteOldTagsRequest(DeleteTagsRequest.builder()
                        .tagKeys(toBeDeletedTags
                                .stream()
                                .map(Tag::getKey)
                                .collect(Collectors.toList()))
                        .resourceName(resourceName)
                        .build())
                .build();
    }

    private static software.amazon.awssdk.services.redshift.model.Tag translateToSdkTag(Tag tag) {
        return GSON.fromJson(GSON.toJson(tag), software.amazon.awssdk.services.redshift.model.Tag.class);
    }

    private static <T> Stream<T> streamOfOrEmpty(final Collection<T> collection) {
        return Optional.ofNullable(collection)
                .map(Collection::stream)
                .orElseGet(Stream::empty);
    }

    public static List<software.amazon.awssdk.services.redshift.model.Tag> translateToSdkTags(List<Tag> modelTags) {
        return Optional.ofNullable(modelTags)
                .map(tags -> tags
                        .stream()
                        .map(Translator::translateToSdkTag)
                        .collect(Collectors.toList()))
                .orElse(null);
    }

    static List<Tag> translateTagsMapToTagCollection(final Map<String, String> tags) {
        if (tags == null) return null;
        return tags.keySet().stream()
                .map(key -> Tag.builder().key(key).value(tags.get(key)).build())
                .collect(Collectors.toList());
    }

    static Map<String, String> translateFromResourceModelToSdkTags(final List<Tag> listOfTags){

        Map<String, String> sdkTags = streamOfOrEmpty(listOfTags)
                .collect(Collectors.toMap(Tag::getKey, Tag::getValue ));

        return sdkTags.isEmpty() ? null : sdkTags;
    }

    private static Tag translateToModelTag(software.amazon.awssdk.services.redshift.model.Tag tag) {
        return GSON.fromJson(GSON.toJson(tag), Tag.class);
    }

    private static List<Tag> translateToModelTags(List<software.amazon.awssdk.services.redshift.model.Tag> sdkTags) {
        return Optional.ofNullable(sdkTags)
                .map(tags -> tags
                        .stream()
                        .map(Translator::translateToModelTag)
                        .collect(Collectors.toList()))
                .orElse(null);
    }

    /**
     * Do subtract for two lists: a - b
     *
     * @param a   the list that wants to minus b
     * @param b   the list that would be subtracted by a
     * @param <T> the class of a's and b's elements
     * @return (a - b) a.k.a (a minus b)
     */
    private static <T> List<T> subtract(List<T> a, List<T> b) {
        return Optional.ofNullable(a)
                .map(aIfNotNull -> aIfNotNull
                        .stream()
                        .filter(ao -> Optional.ofNullable(b)
                                .map(bIfNotNull -> !bIfNotNull.contains(ao))
                                .orElse(true))
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }
}
