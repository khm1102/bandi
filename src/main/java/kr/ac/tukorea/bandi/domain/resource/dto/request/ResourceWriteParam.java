package kr.ac.tukorea.bandi.domain.resource.dto.request;

import kr.ac.tukorea.bandi.domain.resource.model.ResourceTargetScope;

import java.util.List;

public record ResourceWriteParam(
        ResourceTargetScope targetScope,
        Long teamId,
        String categoryCode,
        String title,
        String description,
        boolean pinned,
        List<Long> storedFileIds
) {

    public ResourceWriteParam {
        storedFileIds = storedFileIds == null ? List.of() : List.copyOf(storedFileIds);
    }
}
