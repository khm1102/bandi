package kr.ac.tukorea.bandi.global.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "bandi.profile-photo-retirement")
public record ProfilePhotoRetirementProperties(
        @NotNull ProfilePhotoRetirementMode mode
) {
}
