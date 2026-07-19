package kr.ac.tukorea.bandi.domain.performance.dto.request;

import kr.ac.tukorea.bandi.domain.performance.model.CastType;

public record PerformanceRoundCastAssignParam(
        Long performanceProjectId,
        Long performanceRoundId,
        Long performanceCharacterId,
        Long publicProfileId,
        CastType castType,
        String reason
) {
}
