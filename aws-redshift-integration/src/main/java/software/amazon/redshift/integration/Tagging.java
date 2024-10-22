package software.amazon.redshift.integration;

import com.amazonaws.util.CollectionUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import software.amazon.awssdk.services.redshift.RedshiftClient;
import software.amazon.awssdk.services.redshift.model.CreateTagsRequest;
import software.amazon.awssdk.services.redshift.model.DeleteTagsRequest;
import software.amazon.awssdk.services.redshift.model.Tag;
import software.amazon.cloudformation.proxy.ProxyClient;


/*
copied from
https://github.com/aws-cloudformation/aws-cloudformation-resource-providers-rds/blob/master/aws-rds-cfn-common/src/main/java/software/amazon/rds/common/handler/Tagging.java

TODO: move to a common lib for all Redshift CFN pkgs to consume
 */
public class Tagging {
    public static TagSet exclude(final TagSet from, final TagSet what) {
        final Set<Tag> systemTags = new LinkedHashSet<>(from.getSystemTags());
        systemTags.removeAll(what.getSystemTags());

        final Set<Tag> stackTags = new LinkedHashSet<>(from.getStackTags());
        stackTags.removeAll(what.getStackTags());

        final Set<Tag> resourceTags = new LinkedHashSet<>(from.getResourceTags());
        resourceTags.removeAll(what.getResourceTags());

        return TagSet.builder()
                .systemTags(systemTags)
                .stackTags(stackTags)
                .resourceTags(resourceTags)
                .build();
    }

    public static Collection<Tag> exclude(final Collection<Tag> from, final Collection<Tag> what) {
        final Set<Tag> result = new LinkedHashSet<>(from);
        result.removeAll(what);
        return result;
    }

    public static Set<Tag> translateTagsToSdk(final Collection<Tag> tags) {
        return Optional.ofNullable(tags).orElse(Collections.emptySet())
                .stream()
                .map(tag -> Tag.builder()
                        .key(tag.key())
                        .value(tag.value())
                        .build())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static Set<Tag> translateTagsToSdk(final Set<software.amazon.redshift.integration.Tag> tags) {
        return Optional.ofNullable(tags).orElse(Collections.emptySet())
                .stream()
                .map(tag -> Tag.builder()
                        .key(tag.getKey())
                        .value(tag.getValue())
                        .build())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public static Collection<Tag> translateTagsToSdk(final TagSet tagSet) {
        //For backward compatibility, We will resolve duplicates tags between stack level tags and resource tags.
        final Map<String, Tag> allTags = new LinkedHashMap<>();
        addToMapIfAbsent(allTags, tagSet.getResourceTags());
        addToMapIfAbsent(allTags, tagSet.getStackTags());
        addToMapIfAbsent(allTags, tagSet.getSystemTags());
        return allTags.values();
    }

    public static Set<Tag> translateTagsToSdk(final Map<String, String> tags) {
        return Optional.ofNullable(tags).orElse(Collections.emptySortedMap()).entrySet()
                .stream()
                .map(entry -> Tag.builder()
                        .key(entry.getKey())
                        .value(entry.getValue())
                        .build())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    static Set<software.amazon.redshift.integration.Tag> translateTags(final Collection<Tag> redshiftTags) {
        return CollectionUtils.isNullOrEmpty(redshiftTags) ? null
                : redshiftTags.stream()
                .map(tag -> software.amazon.redshift.integration.Tag.builder()
                        .key(tag.key())
                        .value(tag.value())
                        .build())
                .collect(Collectors.toSet());
    }

    private static void addToMapIfAbsent(Map<String, Tag> allTags, Collection<Tag> tags) {
        for (Tag tag : tags) {
            allTags.putIfAbsent(tag.key(), tag);
        }
    }

    public static void deleteTags(
            final ProxyClient<RedshiftClient> RedshiftClientProxyClient,
            final String arn,
            final Collection<Tag> tagsToRemove
    ) {
        if (CollectionUtils.isNullOrEmpty(tagsToRemove)) {
            return;
        }

        RedshiftClientProxyClient.injectCredentialsAndInvokeV2(
                deleteTagsFromResourceRequest(arn, tagsToRemove),
                RedshiftClientProxyClient.client()::deleteTags
        );
    }

    private static DeleteTagsRequest deleteTagsFromResourceRequest(
            final String arn,
            final Collection<Tag> tagsToRemove
    ) {
        return DeleteTagsRequest.builder()
                .resourceName(arn)
                .tagKeys(tagsToRemove.stream().map(Tag::key).collect(Collectors.toCollection(LinkedHashSet::new)))
                .build();
    }

    public static void createTags(
            final ProxyClient<RedshiftClient> RedshiftClientProxyClient,
            final String arn,
            final Collection<Tag> tagsToAdd
    ) {
        if (CollectionUtils.isNullOrEmpty(tagsToAdd)) {
            return;
        }

        RedshiftClientProxyClient.injectCredentialsAndInvokeV2(
                createTagsToResourceRequest(arn, tagsToAdd),
                RedshiftClientProxyClient.client()::createTags
        );
    }

    private static CreateTagsRequest createTagsToResourceRequest(final String arn, final Collection<Tag> tagsToAdd) {
        return CreateTagsRequest.builder()
                .resourceName(arn)
                .tags(tagsToAdd)
                .build();
    }

    public static Map<String, String> translateTagsToRequest(final Collection<software.amazon.redshift.integration.Tag> tags) {
        return Optional.ofNullable(tags).orElse(Collections.emptyList())
                .stream()
                .collect(Collectors.toMap(software.amazon.redshift.integration.Tag::getKey, software.amazon.redshift.integration.Tag::getValue));
    }

    @Builder(toBuilder = true)
    @AllArgsConstructor
    @Data
    public static class TagSet {
        @Builder.Default
        private Set<Tag> systemTags = new LinkedHashSet<>();
        @Builder.Default
        private Set<Tag> stackTags = new LinkedHashSet<>();
        @Builder.Default
        private Set<Tag> resourceTags = new LinkedHashSet<>();

        public static TagSet emptySet() {
            return TagSet.builder().build();
        }

        public boolean isEmpty() {
            return systemTags.isEmpty() &&
                    stackTags.isEmpty() &&
                    resourceTags.isEmpty();
        }
    }
}
