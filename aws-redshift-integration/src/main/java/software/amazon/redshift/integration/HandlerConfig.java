package software.amazon.redshift.integration;

import lombok.Builder;
import lombok.Getter;
import software.amazon.cloudformation.proxy.delay.Constant;

import java.time.Duration;

@Builder
public class HandlerConfig {
    @Getter
    @Builder.Default
    final private boolean probingEnabled = false;

    @Getter
    @Builder.Default
    final private Constant backoff = Constant.of()
            .delay(Duration.ofSeconds(30))
            .timeout(Duration.ofMinutes(90))
            .build();
}
