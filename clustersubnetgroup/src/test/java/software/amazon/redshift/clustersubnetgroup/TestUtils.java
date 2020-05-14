package software.amazon.redshift.clustersubnetgroup;

import com.google.common.collect.ImmutableMap;
import software.amazon.awssdk.services.redshift.model.ClusterSubnetGroup;
import software.amazon.awssdk.services.redshift.model.DescribeTagsResponse;
import software.amazon.awssdk.services.redshift.model.Subnet;
import software.amazon.awssdk.services.redshift.model.TaggedResource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TestUtils {
    final static String AWS_REGION = "us-east-1";

    final static List<String> SUBNET_IDS = Arrays.asList("subnet-1", "subnet-2");
    final static List<TaggedResource> TAGGED_RESOURCES = Arrays.asList(
            TaggedResource.builder().tag(software.amazon.awssdk.services.redshift.model.Tag.builder().key("key1").value("val1").build()).build(),
            TaggedResource.builder().tag(software.amazon.awssdk.services.redshift.model.Tag.builder().key("key2").value("val2").build()).build(),
            TaggedResource.builder().tag(software.amazon.awssdk.services.redshift.model.Tag.builder().key("key3").value("val3").build()).build()
    );
    final static List<Tag> TAGS = Arrays.asList(new Tag("key1", "val1"), new Tag("key2", "val2"), new Tag("key3", "val3"));
    final static Map<String, String> DESIRED_RESOURCE_TAGS = ImmutableMap.of("key1", "val1", "key2", "val2", "key3", "val3");
    final static List<software.amazon.awssdk.services.redshift.model.ClusterSubnetGroup> SDK_SUBNET_GROUP = Arrays.asList(
            software.amazon.awssdk.services.redshift.model.ClusterSubnetGroup.builder().build());

    final static ResourceModel BASIC_MODEL = ResourceModel.builder()
            .description("description")
            .subnetIds(SUBNET_IDS)
            .build();

    final static DescribeTagsResponse DESCRIBE_TAGS_RESPONSE = DescribeTagsResponse.builder()
            .taggedResources(TAGGED_RESOURCES)
            .build();

    final static ClusterSubnetGroup BASIC_CLUSTER_SUBNET_GROUP = ClusterSubnetGroup.builder()
            .clusterSubnetGroupName("name")
            .description("description")
            .subnets(Subnet.builder().subnetIdentifier("subnet").build(), Subnet.builder().subnetIdentifier("subnet").build())
            .build();
}
