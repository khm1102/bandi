package kr.ac.tukorea.bandi.domain.resource.dto.response;

import kr.ac.tukorea.bandi.domain.resource.model.ResourceStatus;
import kr.ac.tukorea.bandi.domain.resource.model.ResourceTargetScope;

import java.time.LocalDateTime;

public record ResourceManageSummaryResponse(
        Long resourceId,
        ResourceTargetScope targetScope,
        Long teamId,
        String teamName,
        String categoryCode,
        String title,
        ResourceStatus status,
        boolean pinned,
        Integer currentRevisionNo,
        String updatedByName,
        LocalDateTime updatedDttm
) {
}
