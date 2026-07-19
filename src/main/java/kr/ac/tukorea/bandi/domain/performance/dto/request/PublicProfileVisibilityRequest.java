package kr.ac.tukorea.bandi.domain.performance.dto.request;

import jakarta.validation.constraints.NotNull;
import kr.ac.tukorea.bandi.domain.performance.model.PublicProfileVisibility;

public record PublicProfileVisibilityRequest(
        @NotNull PublicProfileVisibility visibilityStatus
) {
}
