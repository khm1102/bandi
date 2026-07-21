package kr.ac.tukorea.bandi.domain.performance.retirement;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bandi.retirement.performance-files")
public record PerformanceFileRetirementProperties(
        boolean enabled,
        PerformanceFileRetirementMode mode
) {

    public PerformanceFileRetirementProperties {
        if (mode == null) {
            mode = PerformanceFileRetirementMode.REPORT;
        }
    }
}
