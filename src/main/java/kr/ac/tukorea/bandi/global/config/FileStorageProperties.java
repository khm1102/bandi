package kr.ac.tukorea.bandi.global.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "bandi.storage")
public record FileStorageProperties(
        @NotBlank String endpoint,
        @NotBlank String accessKey,
        @NotBlank String secretKey,
        @NotBlank String privateBucket,
        @NotBlank String publicBucket,
        @Positive long maxUploadBytes,
        @NotNull Duration privateUrlLifetime
) {

    private static final Duration MAX_PRESIGNED_LIFETIME = Duration.ofDays(7);

    public FileStorageProperties {
        if (privateUrlLifetime != null && (privateUrlLifetime.isZero()
                || privateUrlLifetime.isNegative()
                || privateUrlLifetime.compareTo(MAX_PRESIGNED_LIFETIME) > 0)) {
            throw new IllegalArgumentException("private URL lifetime must be between 1 second and 7 days");
        }
    }
}
