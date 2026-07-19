package kr.ac.tukorea.bandi.domain.resource.dto.response;

import kr.ac.tukorea.bandi.domain.resource.model.ResourceTargetScope;

import java.time.LocalDateTime;

public record ResourceSummaryResponse(
        Long resourceId,
        ResourceTargetScope targetScope,
        Long teamId,
        String teamName,
        String categoryCode,
        String title,
        boolean pinned,
        Integer currentRevisionNo,
        String updatedByName,
        LocalDateTime updatedDttm
) {
}
