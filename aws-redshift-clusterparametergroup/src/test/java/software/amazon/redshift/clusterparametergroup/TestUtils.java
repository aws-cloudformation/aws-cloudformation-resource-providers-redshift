package software.amazon.redshift.clusterparametergroup;

import com.google.common.collect.ImmutableMap;
import software.amazon.awssdk.services.redshift.model.ClusterParameterGroup;
import software.amazon.awssdk.services.redshift.model.DescribeTagsResponse;
import software.amazon.awssdk.services.redshift.model.TaggedResource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TestUtils {
    final static String DESCRIPTION = "description";
    final static String PARAMETER_GROUP_FAMILY = "redshift-1.0";
    final static String PARAMETER_GROUP_NAME = "logicalid-kvw2fztz3cvh";
    final static String AWS_ACCOUNT_ID = "1111";

    final static String AWS_REGION = "us-east-1";

    final static Map<String, String> DESIRED_RESOURCE_TAGS = ImmutableMap.of("key1", "val1", "key2", "val2", "key3", "val3");

    final static List<Tag> TAGS = Arrays.asList(new Tag("key1", "val1"), new Tag("key2", "val2"), new Tag("key3", "val3"));

    final static Map<String, String> PREVIOUS_TAGS = ImmutableMap.of("key4", "val4", "key2", "val2");

    final static List<software.amazon.awssdk.services.redshift.model.Tag> SDK_TAGS = Arrays.asList(
            software.amazon.awssdk.services.redshift.model.Tag.builder().key("key1").value("val1").build(),
            software.amazon.awssdk.services.redshift.model.Tag.builder().key("key2").value("val2").build(),
            software.amazon.awssdk.services.redshift.model.Tag.builder().key("stackKey").value("stackValue").build()
    );

    final static List<TaggedResource> TAGGED_RESOURCES = Arrays.asList(
            TaggedResource.builder().tag(software.amazon.awssdk.services.redshift.model.Tag.builder().key("key1").value("val1").build()).build(),
            TaggedResource.builder().tag(software.amazon.awssdk.services.redshift.model.Tag.builder().key("key2").value("val2").build()).build(),
            TaggedResource.builder().tag(software.amazon.awssdk.services.redshift.model.Tag.builder().key("key3").value("val3").build()).build()
    );

    final static String WLM_JSON_CONFIGURATION = "[{\"user_group\":\"example_user_group1\",\"query_group\": \"example_query_group1\", \"query_concurrency\":7},{\"query_concurrency\":5}]";
    final static List<Parameter> DESIRED_PARAMETERS = Arrays.asList(
            Parameter.builder()
                    .parameterName("auto_analyze")
                    .parameterValue("true")
                    .build(),
            Parameter.builder()
                    .parameterName("datestyle")
                    .parameterValue("ISO, MDY")
                    .build(),
            Parameter.builder()
                    .parameterName("wlm_json_configuration")
                    .parameterValue(WLM_JSON_CONFIGURATION)
                    .build()
    );

    final static List<Parameter> PREVIOUS_PARAMETERS = Arrays.asList(
            Parameter.builder()
                    .parameterName("datestyle")
                    .parameterValue("ISO, MDY")
                    .build(),
            Parameter.builder()
                    .parameterName("wlm_json_configuration")
                    // adding format changes to the json string,
                    // we'll need to compare the actual JSON objects are equal functionally
                    .parameterValue("[ {\"user_group\":\"example_user_group1\",\"query_group\":\"example_query_group1\",  \"query_concurrency\":7},{\"query_concurrency\":5}]")
                    .build()
    );

    final static List<software.amazon.awssdk.services.redshift.model.Parameter> SDK_PARAMETERS = Arrays.asList(
            software.amazon.awssdk.services.redshift.model.Parameter.builder().parameterName("auto_analyze").parameterValue("true").isModifiable(true).build(),
            software.amazon.awssdk.services.redshift.model.Parameter.builder().parameterName("datestyle").parameterValue("ISO, MDY").isModifiable(true).build(),
            software.amazon.awssdk.services.redshift.model.Parameter.builder().parameterName("statement_timeout").parameterValue("1000").isModifiable(true).build()
    );

    final static List<software.amazon.awssdk.services.redshift.model.Parameter> getSdkParametersFromParameters(List<Parameter> parameters) {
        return parameters.stream().map(param -> software.amazon.awssdk.services.redshift.model.Parameter.builder()
                .parameterName(param.getParameterName())
                .parameterValue(param.getParameterValue())
                .source("user")
                .isModifiable(true)
                .build()).collect(Collectors.toList());
    }

    final static ResourceModel COMPLETE_MODEL = ResourceModel.builder()
            .parameterGroupName(PARAMETER_GROUP_NAME)
            .description(DESCRIPTION)
            .parameterGroupFamily(PARAMETER_GROUP_FAMILY)
            .tags(TAGS)
            .parameters(DESIRED_PARAMETERS)
            .build();

    final static ClusterParameterGroup CLUSTER_PARAMETER_GROUP = ClusterParameterGroup.builder()
            .parameterGroupName(PARAMETER_GROUP_NAME)
            .parameterGroupFamily(PARAMETER_GROUP_FAMILY)
            .description(DESCRIPTION)
            .tags(SDK_TAGS)
            .build();

    final static List<TaggedResource> TAGGED_RESOURCES_CREATING = Arrays.asList(
            TaggedResource.builder().tag(software.amazon.awssdk.services.redshift.model.Tag.builder().key("key1").value("val1_create").build()).build(),
            TaggedResource.builder().tag(software.amazon.awssdk.services.redshift.model.Tag.builder().key("key3").value("val3").build()).build(),
            TaggedResource.builder().tag(software.amazon.awssdk.services.redshift.model.Tag.builder().key("stackKey").value("stackValueCreated").build()).build()
    );

    final static List<software.amazon.awssdk.services.redshift.model.Tag> SDK_TAGS_TO_CREATE = Arrays.asList(
            software.amazon.awssdk.services.redshift.model.Tag.builder().key("key1").value("val1").build(),
            software.amazon.awssdk.services.redshift.model.Tag.builder().key("key2").value("val2").build(),
            software.amazon.awssdk.services.redshift.model.Tag.builder().key("stackKey").value("stackValue").build()
    );

    final static DescribeTagsResponse DESCRIBE_TAGS_RESPONSE_CREATING = DescribeTagsResponse.builder()
            .taggedResources(TAGGED_RESOURCES_CREATING)
            .build();

    final static boolean parametersEquals(
            List<software.amazon.awssdk.services.redshift.model.Parameter> params1,
            List<software.amazon.awssdk.services.redshift.model.Parameter> params2
    ) {
        if (params1.size() != params2.size()) {
            return false;
        }

        Function<List<software.amazon.awssdk.services.redshift.model.Parameter>, Set<String>> paramsToSet = list -> list.stream()
                .map(parameter -> parameter.parameterName() + parameter.parameterValue())
                .collect(Collectors.toCollection(TreeSet::new));

        Set<String> set1 = paramsToSet.apply(params1);
        Set<String> set2 = paramsToSet.apply(params2);

        return set1.equals(set2);
    }
}
