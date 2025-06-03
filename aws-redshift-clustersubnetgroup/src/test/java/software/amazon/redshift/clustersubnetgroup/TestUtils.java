package software.amazon.redshift.clustersubnetgroup;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetGroup;
import software.amazon.awssdk.services.redshift.model.CreateTagsRequest;
import software.amazon.awssdk.services.redshift.model.DeleteTagsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeTagsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeTagsResponse;
import software.amazon.awssdk.services.redshift.model.Subnet;
import software.amazon.awssdk.services.redshift.model.Tag;
import software.amazon.awssdk.services.redshift.model.TaggedResource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TestUtils {
    // Basic constants
    final static String AWS_REGION = "us-east-1";
    final static String DESCRIPTION = "description";
    final static String SUBNET_GROUP_NAME = "logicalid-kvw2fztz3cvh";
    final static String AWS_ACCOUNT_ID = "1111";
    final static String ARN = String.format("arn:aws:redshift:%s:%s:subnetgroup:%s",
            AWS_REGION, AWS_ACCOUNT_ID, SUBNET_GROUP_NAME);

    // Subnet IDs
    final static List<String> SUBNET_IDS = Arrays.asList("subnet-1", "subnet-2");

    // Tags
    final static Map<String, String> DESIRED_RESOURCE_TAGS = ImmutableMap.of(
            "key1", "val1",
            "key2", "val2",
            "key3", "val3"
    );

    final static Map<String, String> PREVIOUS_RESOURCE_TAGS = ImmutableMap.of(
            "key4", "val4",
            "key2", "val2"
    );

    final static List<software.amazon.redshift.clustersubnetgroup.Tag> TAGS = Arrays.asList(
            new software.amazon.redshift.clustersubnetgroup.Tag("key1", "val1"),
            new software.amazon.redshift.clustersubnetgroup.Tag("key2", "val2"),
            new software.amazon.redshift.clustersubnetgroup.Tag("key3", "val3")
    );

    final static List<Tag> SDK_TAGS = Arrays.asList(
            Tag.builder().key("key1").value("val1").build(),
            Tag.builder().key("key2").value("val2").build(),
            Tag.builder().key("stackKey").value("stackValue").build()
    );

    // Resource Models
    final static ResourceModel BASIC_MODEL = ResourceModel.builder()
            .description(DESCRIPTION)
            .clusterSubnetGroupName(SUBNET_GROUP_NAME)
            .subnetIds(SUBNET_IDS)
            .tags(Collections.emptyList())
            .build();

    final static ResourceModel MODEL_WITH_TAGS = ResourceModel.builder()
            .description(DESCRIPTION)
            .clusterSubnetGroupName(SUBNET_GROUP_NAME)
            .subnetIds(SUBNET_IDS)
            .tags(TAGS)
            .build();

    final static ResourceModel PREVIOUS_MODEL = ResourceModel.builder()
            .description("old-description")
            .clusterSubnetGroupName(SUBNET_GROUP_NAME)
            .subnetIds(Arrays.asList("subnet-old"))
            .tags(Arrays.asList(
                    new software.amazon.redshift.clustersubnetgroup.Tag("key4", "val4"),
                    new software.amazon.redshift.clustersubnetgroup.Tag("key2", "val2")
            ))
            .build();

    // Subnet Groups
    final static ClusterSubnetGroup BASIC_CLUSTER_SUBNET_GROUP = ClusterSubnetGroup.builder()
            .clusterSubnetGroupName(SUBNET_GROUP_NAME)
            .description(DESCRIPTION)
            .subnets(Arrays.asList(
                    Subnet.builder().subnetIdentifier(SUBNET_IDS.get(0)).build(),
                    Subnet.builder().subnetIdentifier(SUBNET_IDS.get(1)).build()
            ))
            .tags(SDK_TAGS)
            .build();

    final static ClusterSubnetGroup UPDATED_CLUSTER_SUBNET_GROUP = ClusterSubnetGroup.builder()
            .clusterSubnetGroupName(SUBNET_GROUP_NAME)
            .description(DESCRIPTION)
            .subnets(Arrays.asList(
                    Subnet.builder().subnetIdentifier(SUBNET_IDS.get(0)).build(),
                    Subnet.builder().subnetIdentifier(SUBNET_IDS.get(1)).build()
            ))
            .tags(SDK_TAGS)
            .build();

    // Tagged Resources
    final static List<TaggedResource> TAGGED_RESOURCES = Arrays.asList(
            TaggedResource.builder().tag(Tag.builder().key("key1").value("val1").build()).build(),
            TaggedResource.builder().tag(Tag.builder().key("key2").value("val2").build()).build(),
            TaggedResource.builder().tag(Tag.builder().key("key3").value("val3").build()).build()
    );

    final static List<TaggedResource> TAGGED_RESOURCES_CREATING = Arrays.asList(
            TaggedResource.builder().tag(Tag.builder().key("key1").value("val1_create").build()).build(),
            TaggedResource.builder().tag(Tag.builder().key("key3").value("val3").build()).build(),
            TaggedResource.builder().tag(Tag.builder().key("stackKey").value("stackValueCreated").build()).build()
    );

    // Tag Operations
    final static List<Tag> SDK_TAGS_TO_CREATE = Arrays.asList(
            Tag.builder().key("key1").value("val1").build(),
            Tag.builder().key("key3").value("val3").build()
    );

    final static List<String> SDK_TAGS_TO_DELETE = ImmutableList.of("key4");

    // Responses
    final static DescribeTagsResponse DESCRIBE_TAGS_RESPONSE = DescribeTagsResponse.builder()
            .taggedResources(TAGGED_RESOURCES)
            .build();

    final static DescribeTagsResponse DESCRIBE_TAGS_RESPONSE_CREATING = DescribeTagsResponse.builder()
            .taggedResources(TAGGED_RESOURCES_CREATING)
            .build();

    final static DescribeTagsResponse DESCRIBE_TAGS_RESPONSE_UPDATING = DescribeTagsResponse.builder()
            .taggedResources(Arrays.asList(
                    TaggedResource.builder()
                            .tag(Tag.builder().key("key2").value("val2").build())
                            .build(),
                    TaggedResource.builder()
                            .tag(Tag.builder().key("key4").value("val4").build())
                            .build()
            ))
            .build();

    // Requests
    final static CreateTagsRequest CREATE_TAGS_REQUEST = CreateTagsRequest.builder()
            .resourceName(ARN)
            .tags(SDK_TAGS_TO_CREATE)
            .build();

    final static DeleteTagsRequest DELETE_TAGS_REQUEST = DeleteTagsRequest.builder()
            .resourceName(ARN)
            .tagKeys(SDK_TAGS_TO_DELETE)
            .build();

    final static DescribeTagsRequest DESCRIBE_TAGS_REQUEST = DescribeTagsRequest.builder()
            .resourceName(ARN)
            .build();

    // Invalid Models
    final static ResourceModel INVALID_SUBNET_MODEL = ResourceModel.builder()
            .description(DESCRIPTION)
            .clusterSubnetGroupName(SUBNET_GROUP_NAME)
            .subnetIds(Arrays.asList("invalid-subnet-1"))
            .tags(TAGS)
            .build();

    // Helper Methods
    static ResourceModel buildModelWithTags(List<software.amazon.redshift.clustersubnetgroup.Tag> tags) {
        return ResourceModel.builder()
                .description(DESCRIPTION)
                .clusterSubnetGroupName(SUBNET_GROUP_NAME)
                .subnetIds(SUBNET_IDS)
                .tags(tags)
                .build();
    }
}