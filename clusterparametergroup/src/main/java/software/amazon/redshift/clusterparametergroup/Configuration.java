package software.amazon.redshift.clusterparametergroup;

import java.util.Map;
import org.json.JSONObject;
import org.json.JSONTokener;

class Configuration extends BaseConfiguration {

    public Configuration() {
        super("aws-redshift-clusterparametergroup.json");
    }
}
