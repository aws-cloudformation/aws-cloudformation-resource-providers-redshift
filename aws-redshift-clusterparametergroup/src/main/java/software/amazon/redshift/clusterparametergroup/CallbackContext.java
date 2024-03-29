package software.amazon.redshift.clusterparametergroup;

import software.amazon.cloudformation.proxy.StdCallbackContext;

@lombok.Getter
@lombok.Setter
@lombok.ToString
@lombok.EqualsAndHashCode(callSuper = true)
public class CallbackContext extends StdCallbackContext {
    private boolean parametersApplied;
    private String marker;
    private boolean clusterStabilized;
}
