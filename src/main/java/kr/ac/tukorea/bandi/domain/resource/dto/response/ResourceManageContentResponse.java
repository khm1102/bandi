package kr.ac.tukorea.bandi.domain.resource.dto.response;

import kr.ac.tukorea.bandi.domain.resource.model.ResourceStatus;
import kr.ac.tukorea.bandi.domain.resource.model.ResourceTargetScope;

import java.time.LocalDateTime;

public record ResourceManageContentResponse(
        Long resourceId,
        ResourceTargetScope targetScope,
        Long teamId,
        String teamName,
        String categoryCode,
        String title,
        String description,
        ResourceStatus status,
        boolean pinned,
        String createdByName,
        String updatedByName,
        LocalDateTime updatedDttm
) {
}
