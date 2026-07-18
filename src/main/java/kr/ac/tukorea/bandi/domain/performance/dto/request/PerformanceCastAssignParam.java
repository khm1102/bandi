package kr.ac.tukorea.bandi.domain.performance.dto.request;

import kr.ac.tukorea.bandi.domain.performance.model.CastType;

public record PerformanceCastAssignParam(
        Long performanceProjectId,
        Long performanceCharacterId,
        Long publicProfileId,
        CastType castType,
        int displayOrder,
        String reason
) {
}
