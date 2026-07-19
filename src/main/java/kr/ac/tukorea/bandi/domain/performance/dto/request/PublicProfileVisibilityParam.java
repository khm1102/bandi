package kr.ac.tukorea.bandi.domain.performance.dto.request;

import kr.ac.tukorea.bandi.domain.performance.model.PublicProfileVisibility;

public record PublicProfileVisibilityParam(
        Long publicProfileId,
        PublicProfileVisibility visibilityStatus
) {
}
