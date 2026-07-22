package kr.ac.tukorea.bandi.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bandi.retirement.public-notice")
public record PublicNoticeRetirementProperties(
        PublicNoticeRetirementMode mode
) {

    public PublicNoticeRetirementProperties {
        if (mode == null) {
            mode = PublicNoticeRetirementMode.OFF;
        }
    }
}
