package kr.ac.tukorea.bandi.global.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "bandi.school-login-attempt")
public record SchoolLoginAttemptProperties(
        @Positive int maxFailures,
        @NotNull Duration failureWindow,
        @NotNull Duration cooldown,
        @NotBlank String hashSecret
) {

    public SchoolLoginAttemptProperties {
        requirePositive(failureWindow, "failureWindow");
        requirePositive(cooldown, "cooldown");
    }

    private static void requirePositive(Duration value, String propertyName) {
        if (value != null && (value.isZero() || value.isNegative())) {
            throw new IllegalArgumentException(propertyName + " must be positive");
        }
    }
}
