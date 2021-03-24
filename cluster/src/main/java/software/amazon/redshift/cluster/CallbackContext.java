package software.amazon.redshift.cluster;

import software.amazon.cloudformation.proxy.StdCallbackContext;

@lombok.Getter
@lombok.Setter
@lombok.ToString
@lombok.EqualsAndHashCode(callSuper = true)
public class CallbackContext extends StdCallbackContext {
    LoggingProperties loggingProperties;

    public void setLoggingProperties(LoggingProperties loggingProperties) {
        this.loggingProperties = loggingProperties;
    }

    public LoggingProperties getLoggingProperties() {
        return loggingProperties;
    }
}
