package kr.ac.tukorea.bandi.domain.resource.dto.response;

import kr.ac.tukorea.bandi.domain.resource.model.ResourceStatus;
import kr.ac.tukorea.bandi.domain.resource.model.ResourceTargetScope;

import java.time.LocalDateTime;
import java.util.List;

public record ResourceManageDetailResponse(
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
        LocalDateTime updatedDttm,
        List<ResourceRevisionResponse> revisions
) {

    public static ResourceManageDetailResponse of(
            ResourceManageContentResponse content,
            List<ResourceRevisionResponse> revisions) {
        return new ResourceManageDetailResponse(content.resourceId(),
                content.targetScope(), content.teamId(), content.teamName(),
                content.categoryCode(), content.title(), content.description(),
                content.status(), content.pinned(), content.createdByName(),
                content.updatedByName(), content.updatedDttm(), List.copyOf(revisions));
    }
}
