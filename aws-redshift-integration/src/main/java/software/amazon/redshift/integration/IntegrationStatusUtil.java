package software.amazon.redshift.integration;

import com.google.common.collect.ImmutableSet;
import software.amazon.awssdk.services.redshift.model.ZeroETLIntegrationStatus;

import java.util.Set;

public class IntegrationStatusUtil {
    private static final Set<ZeroETLIntegrationStatus> VALID_CREATING_STATES = ImmutableSet.of(
            ZeroETLIntegrationStatus.CREATING,
            ZeroETLIntegrationStatus.SYNCING,
            ZeroETLIntegrationStatus.MODIFYING,
            ZeroETLIntegrationStatus.NEEDS_ATTENTION,
            ZeroETLIntegrationStatus.ACTIVE
    );

    private static final Set<ZeroETLIntegrationStatus> STABILIZED_STATES = ImmutableSet.of(
            ZeroETLIntegrationStatus.NEEDS_ATTENTION,
            ZeroETLIntegrationStatus.ACTIVE
    );


    public static boolean isValidCreatingStatus(ZeroETLIntegrationStatus status) {
        return VALID_CREATING_STATES.contains(status);
    }

    public static boolean isStabilizedState(ZeroETLIntegrationStatus status) {
        return STABILIZED_STATES.contains(status);
    }
}
