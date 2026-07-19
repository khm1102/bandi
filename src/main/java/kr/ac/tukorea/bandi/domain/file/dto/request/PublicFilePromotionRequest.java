package kr.ac.tukorea.bandi.domain.file.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PublicFilePromotionRequest(
        @NotBlank
        @Pattern(regexp = "[a-z][a-z0-9-]{1,29}")
        String domain
) {
}
