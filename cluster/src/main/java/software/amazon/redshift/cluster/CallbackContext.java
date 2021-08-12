package software.amazon.redshift.cluster;

import software.amazon.cloudformation.proxy.StdCallbackContext;

@lombok.Getter
@lombok.Setter
@lombok.ToString
@lombok.EqualsAndHashCode(callSuper = true)
public class CallbackContext extends StdCallbackContext {
    LoggingProperties loggingProperties;
    boolean callBackForReboot = false;
    boolean callBackForDelete = false;
    boolean callBackAfterResize = false;
    boolean clusterExistsCheck = false;
    int retryForAquaStabilize = 0;
    boolean callbackAfterAquaModify = false;

    public void setLoggingProperties(LoggingProperties loggingProperties) {
        this.loggingProperties = loggingProperties;
    }

    public LoggingProperties getLoggingProperties() {
        return loggingProperties;
    }

    public void setCallBackForReboot(boolean callBackForReboot) {
        this.callBackForReboot = callBackForReboot;
    }

    public boolean getCallBackForReboot() { return callBackForReboot; }

    public void setCallBackForDelete(boolean callBackForDelete) {
        this.callBackForDelete = callBackForDelete;
    }

    public boolean getCallBackForDelete() {
        return callBackForDelete;
    }

    public void setCallBackAfterResize(boolean callBackAfterResize) {
        this.callBackAfterResize = callBackAfterResize;
    }

    public boolean getCallBackAfterResize() { return callBackAfterResize; }

    public boolean getClusterExistsCheck() { return this.clusterExistsCheck; }

    public void setClusterExistsCheck(boolean clusterExistsCheck) { this.clusterExistsCheck = clusterExistsCheck; }

    public int getRetryForAquaStabilize() { return this.retryForAquaStabilize; }

    public void setRetryForAquaStabilize(int retryForAquaStabilize) { this.retryForAquaStabilize = retryForAquaStabilize; }

    public boolean getCallbackAfterAquaModify() { return this.callbackAfterAquaModify; }

    public void setCallbackAfterAquaModify(boolean callbackAfterAquaModify) { this.callbackAfterAquaModify = callbackAfterAquaModify; }
}
