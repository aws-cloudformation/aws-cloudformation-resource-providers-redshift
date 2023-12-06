package software.amazon.redshift.cluster;

import software.amazon.cloudformation.proxy.StdCallbackContext;

@lombok.Getter
@lombok.Setter
@lombok.ToString
@lombok.EqualsAndHashCode(callSuper = true)
public class CallbackContext extends StdCallbackContext {
    String namespaceArn = null;
    LoggingProperties loggingProperties;
    boolean callBackForReboot = false;
    boolean callBackForDelete = false;
    boolean callBackAfterResize = false;
    boolean clusterExistsCheck = false;
    int retryForAquaStabilize = 0;
    int retryForPatchingStabilize = 0;
    boolean callbackAfterAquaModify = false;
    boolean callbackAfterClusterMaintenance = false;
    boolean callbackAfterClusterCreate = false;
    boolean callbackAfterClusterRestore = false;
    boolean callbackAfterAfterClusterParameterGroupNameModify = false;

    public void setNamespaceArn(String namespaceArn) {this.namespaceArn = namespaceArn; }

    public String getNamespaceArn() { return namespaceArn; }

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

    public boolean getCallbackAfterClusterParameterGroupNameModify() { return this.callbackAfterAfterClusterParameterGroupNameModify; }

    public void setCallbackAfterClusterParameterGroupNameModify(boolean callbackAfterAfterClusterParameterGroupNameModify) {
        this.callbackAfterAfterClusterParameterGroupNameModify = callbackAfterAfterClusterParameterGroupNameModify;
    }

    public void setCallbackAfterAquaModify(boolean callbackAfterAquaModify) { this.callbackAfterAquaModify = callbackAfterAquaModify; }

    public int getRetryForPatchingStabilize() { return this.retryForPatchingStabilize; }

    public void setRetryForPatchingStabilize(int retryForPatchingStabilize) { this.retryForPatchingStabilize = retryForPatchingStabilize; }

    public boolean getCallbackAfterClusterMaintenance() { return this.callbackAfterClusterMaintenance; }

    public void setCallbackAfterClusterMaintenance(boolean callbackAfterClusterMaintenance) { this.callbackAfterClusterMaintenance = callbackAfterClusterMaintenance; }

    public boolean getCallbackAfterClusterCreate() { return this.callbackAfterClusterCreate; }

    public void setCallbackAfterClusterCreate(boolean callbackAfterClusterCreate) { this.callbackAfterClusterCreate = callbackAfterClusterCreate; }

    public boolean getCallbackAfterClusterRestore() { return this.callbackAfterClusterRestore; }

    public void setCallbackAfterClusterRestore(boolean callbackAfterClusterRestore) { this.callbackAfterClusterRestore = callbackAfterClusterRestore; }

}
