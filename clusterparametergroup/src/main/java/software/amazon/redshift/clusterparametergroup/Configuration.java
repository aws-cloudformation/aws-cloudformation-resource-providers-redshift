package software.amazon.redshift.clusterparametergroup;

import java.util.Map;
import java.util.stream.Collectors;

class Configuration extends BaseConfiguration {

    public Configuration() {
        super("aws-redshift-clusterparametergroup.json");
    }

    public Map<String, String> resourceDefinedTags(final ResourceModel resourceModel) {
        if (resourceModel.getTags() != null) {
            return resourceModel.getTags().stream().collect(Collectors.toMap(tag -> tag.getKey(), tag -> tag.getValue()));
        }
        return null;
    }
}