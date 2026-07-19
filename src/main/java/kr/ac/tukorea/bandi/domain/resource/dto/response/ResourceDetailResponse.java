package kr.ac.tukorea.bandi.domain.resource.dto.response;

import kr.ac.tukorea.bandi.domain.resource.model.ResourceTargetScope;

import java.time.LocalDateTime;
import java.util.List;

public record ResourceDetailResponse(
        Long resourceId,
        ResourceTargetScope targetScope,
        Long teamId,
        String teamName,
        String categoryCode,
        String title,
        String description,
        boolean pinned,
        String updatedByName,
        LocalDateTime updatedDttm,
        List<ResourceFileResponse> files
) {

    public static ResourceDetailResponse of(ResourceContentResponse content,
                                            List<ResourceFileResponse> files) {
        return new ResourceDetailResponse(content.resourceId(), content.targetScope(),
                content.teamId(), content.teamName(), content.categoryCode(),
                content.title(), content.description(), content.pinned(),
                content.updatedByName(), content.updatedDttm(), List.copyOf(files));
    }
}
