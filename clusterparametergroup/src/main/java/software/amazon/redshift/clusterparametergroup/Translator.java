package software.amazon.redshift.clusterparametergroup;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import software.amazon.awssdk.services.redshift.model.CreateClusterParameterGroupRequest;
import software.amazon.awssdk.services.redshift.model.CreateTagsRequest;
import software.amazon.awssdk.services.redshift.model.DeleteClusterParameterGroupRequest;
import software.amazon.awssdk.services.redshift.model.DeleteTagsRequest;
import software.amazon.awssdk.services.redshift.model.DescribeTagsRequest;
import software.amazon.awssdk.services.redshift.model.ModifyClusterParameterGroupRequest;
import software.amazon.awssdk.services.redshift.model.Parameter;
import software.amazon.awssdk.services.redshift.model.ResetClusterParameterGroupRequest;
import software.amazon.awssdk.services.redshift.model.Tag;

public class Translator {
    static List<Tag> translateTagsToSdk(final Map<String, String> tags) {
        if (tags == null) return null;
        return tags.keySet().stream()
                .map(key -> Tag.builder().key(key).value(tags.get(key)).build())
                .collect(Collectors.toList());
    }

    static List<Tag> translateTagsToSdk(final Collection<software.amazon.redshift.clusterparametergroup.Tag> tags) {
        if (tags == null) return null;
        return tags.stream()
                .map(tag -> Tag.builder().key(tag.getKey()).value(tag.getValue()).build())
                .collect(Collectors.toList());
    }

    static List<software.amazon.redshift.clusterparametergroup.Tag> translateTagsFromSdk(final List<Tag> tags) {
        if (tags == null) return null;
        return tags.stream()
                .map(tag -> new software.amazon.redshift.clusterparametergroup.Tag(tag.key(), tag.value()))
                .collect(Collectors.toList());
    }

    private static List<Parameter> translateParametersToSdk(final List<software.amazon.redshift.clusterparametergroup.Parameter> parameters) {
        if (parameters == null) return null;
        return parameters.stream()
                .map(param -> Parameter.builder().parameterName(param.getParameterName()).parameterValue(param.getParameterValue()).build())
                .collect(Collectors.toList());
    }

    static CreateClusterParameterGroupRequest createClusterParameterGroupRequest(final ResourceModel model, final List<Tag> tags) {
        return CreateClusterParameterGroupRequest.builder()
                .parameterGroupName(model.getParameterGroupName())
                .description(model.getDescription())
                .parameterGroupFamily(model.getParameterGroupFamily())
                .tags(tags)
                .build();
    }

    static ModifyClusterParameterGroupRequest modifyClusterParameterGroupRequest(final ResourceModel model) {
        return ModifyClusterParameterGroupRequest.builder()
                .parameterGroupName(model.getParameterGroupName())
                .parameters(translateParametersToSdk(model.getParameters()))
                .build();
    }

    static ResetClusterParameterGroupRequest resetClusterParameterGroupRequest(final ResourceModel model) {
        return ResetClusterParameterGroupRequest.builder()
                .parameterGroupName(model.getParameterGroupName())
                .resetAllParameters(true)
                .build();
    }

    static DeleteClusterParameterGroupRequest deleteClusterParameterGroupRequest(final ResourceModel model) {
        return DeleteClusterParameterGroupRequest.builder()
                .parameterGroupName(model.getParameterGroupName())
                .build();
    }

    static CreateTagsRequest createTagsRequest(final Collection<Tag> tags, final String arn) {
        return CreateTagsRequest.builder()
                .resourceName(arn)
                .tags(tags)
                .build();
    }

    static DeleteTagsRequest deleteTagsRequest(final Collection<Tag> tags, final String arn) {
        return DeleteTagsRequest.builder()
                .resourceName(arn)
                .tagKeys(tags.stream().map(tag -> tag.key()).collect(Collectors.toList()))
                .build();
    }

    static DescribeTagsRequest describeTagsRequest(final String arn) {
        return DescribeTagsRequest.builder()
                .resourceName(arn)
                .build();
    }
}
