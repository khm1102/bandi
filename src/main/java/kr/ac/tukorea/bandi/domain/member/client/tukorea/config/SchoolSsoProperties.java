package kr.ac.tukorea.bandi.domain.member.client.tukorea.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "bandi.school-sso")
public record SchoolSsoProperties(
        @NotNull URI loginPageUrl,
        @NotNull URI loginProcessUrl,
        @NotNull Duration connectTimeout,
        @NotNull Duration requestTimeout,
        @NotBlank String userAgent
) {

    public SchoolSsoProperties {
        requirePositive(connectTimeout, "connectTimeout");
        requirePositive(requestTimeout, "requestTimeout");
    }

    private static void requirePositive(Duration value, String propertyName) {
        if (value != null && (value.isZero() || value.isNegative())) {
            throw new IllegalArgumentException(propertyName + " must be positive");
        }
    }
}
