package kr.ac.tukorea.bandi.domain.resource.dto.request;

import kr.ac.tukorea.bandi.domain.resource.model.ResourceTargetScope;

public record ResourceUpdateParam(
        Long resourceId,
        ResourceTargetScope targetScope,
        Long teamId,
        String categoryCode,
        String title,
        String description,
        boolean pinned
) {
}
