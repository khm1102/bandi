package kr.ac.tukorea.bandi.domain.performance.dto.response;

import kr.ac.tukorea.bandi.domain.performance.model.PublicProfileVisibility;

import java.time.LocalDateTime;

public record PublicProfileResponse(
        Long publicProfileId,
        Long memberId,
        String publicName,
        String bio,
        Long profileFileId,
        String socialUrl,
        PublicProfileVisibility visibilityStatus,
        LocalDateTime createdDttm,
        LocalDateTime updatedDttm
) {
}
