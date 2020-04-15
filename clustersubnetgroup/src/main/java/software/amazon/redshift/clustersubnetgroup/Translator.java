package software.amazon.redshift.clustersubnetgroup;

import com.amazonaws.util.CollectionUtils;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetGroup;
import software.amazon.awssdk.services.redshift.model.CreateClusterSubnetGroupRequest;
import software.amazon.awssdk.services.redshift.model.CreateTagsRequest;
import software.amazon.awssdk.services.redshift.model.DeleteClusterSubnetGroupRequest;
import software.amazon.awssdk.services.redshift.model.DeleteTagsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSubnetGroupsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeClusterSubnetGroupsResponse;
import software.amazon.awssdk.services.redshift.model.DescribeTagsRequest;
import software.amazon.awssdk.services.redshift.model.ModifyClusterSubnetGroupRequest;
import software.amazon.awssdk.services.redshift.model.Subnet;
import software.amazon.awssdk.services.redshift.model.Tag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Translator {
    static List<Tag> translateTagsToSdk(final Map<String, String> tags) {
        if (tags == null) return null;
        return tags.keySet().stream()
                .map(key -> Tag.builder().key(key).value(tags.get(key)).build())
                .collect(Collectors.toList());
    }

    static List<Tag> translateTagsToSdk(final Collection<software.amazon.redshift.clustersubnetgroup.Tag> tags) {
        return tags.stream()
                .map(tag -> Tag.builder().key(tag.getKey()).value(tag.getValue()).build())
                .collect(Collectors.toList());
    }

    static CreateClusterSubnetGroupRequest createClusterSubnetGroupRequest(final ResourceModel model, final List<Tag> tags) {
        return CreateClusterSubnetGroupRequest.builder()
                .clusterSubnetGroupName(model.getSubnetGroupName())
                .subnetIds(model.getSubnetIds())
                .description(model.getDescription())
                .tags(tags)
                .build();
    }

    static ModifyClusterSubnetGroupRequest modifyClusterSubnetGroupRequest(final ResourceModel model) {
        return ModifyClusterSubnetGroupRequest.builder()
                .clusterSubnetGroupName(model.getSubnetGroupName())
                .subnetIds(model.getSubnetIds())
                .description(model.getDescription())
                .build();
    }

    static DescribeClusterSubnetGroupsRequest readClusterSubnetGroupsRequest(final ResourceModel model) {
        return DescribeClusterSubnetGroupsRequest.builder()
                .clusterSubnetGroupName(model.getSubnetGroupName())
                .build();
    }

    static DescribeClusterSubnetGroupsRequest listClusterSubnetGroupsRequest(final ResourceModel model) {
        return DescribeClusterSubnetGroupsRequest.builder().build();
    }

    static DeleteClusterSubnetGroupRequest deleteClusterSubnetGroupRequest(final ResourceModel model) {
        return DeleteClusterSubnetGroupRequest.builder()
                .clusterSubnetGroupName(model.getSubnetGroupName())
                .build();
    }

    static Set<String> getTagsKeySet(final Collection<Tag> tags) {
        return tags.stream().map(tag -> tag.key()).collect(Collectors.toSet());
    }

    static List<Tag> translateTagsToSdk(final Map<String, String> desiredResourceTags, List<software.amazon.redshift.clustersubnetgroup.Tag> resourceTags){
        List<Tag> tags =  new ArrayList<>();
        if (desiredResourceTags != null && !desiredResourceTags.isEmpty()) {
            tags.addAll(translateTagsToSdk(desiredResourceTags));
        }
        if (!CollectionUtils.isNullOrEmpty(resourceTags)) {
            tags.addAll(translateTagsToSdk(resourceTags));
        }
        return tags;
    }

    static CreateTagsRequest createTagsRequest(final Collection<Tag> tags, final String arn) {
        return CreateTagsRequest.builder()
                .resourceName(arn)
                .tags(tags)
                .build();
    }

    static DeleteTagsRequest deleteTagsRequest(final Collection<String> tagsKey, final String arn) {
        return DeleteTagsRequest.builder()
                .resourceName(arn)
                .tagKeys(tagsKey)
                .build();
    }

    static DescribeTagsRequest describeTagsRequest(final String arn) {
        return DescribeTagsRequest.builder()
                .resourceName(arn)
                .build();
    }

    static ResourceModel buildReadResponseModel(DescribeClusterSubnetGroupsResponse response) {
        ClusterSubnetGroup clusterSubnetGroup = response.clusterSubnetGroups().get(0);
        return ResourceModel.builder()
                .description(clusterSubnetGroup.description())
                .subnetGroupName(clusterSubnetGroup.clusterSubnetGroupName())
                .subnetIds(translateSubnetIdsFromSdk(clusterSubnetGroup.subnets()))
                .build();
    }

    static List<ResourceModel> buildListResponseModel(DescribeClusterSubnetGroupsResponse response) {
        List<ResourceModel> models = new ArrayList<>();
        response.clusterSubnetGroups().forEach(clusterSubnetGroup ->{
            ResourceModel model = ResourceModel.builder()
                    .description(clusterSubnetGroup.description())
                    .subnetGroupName(clusterSubnetGroup.clusterSubnetGroupName())
                    .subnetIds(translateSubnetIdsFromSdk(clusterSubnetGroup.subnets()))
                    .build();
            models.add(model);
        });
        return models;
    }


    static List<String> translateSubnetIdsFromSdk (final List<Subnet> subnets) {
        return subnets.stream().map(subnet -> subnet.subnetIdentifier()).collect(Collectors.toList());

    }
}
