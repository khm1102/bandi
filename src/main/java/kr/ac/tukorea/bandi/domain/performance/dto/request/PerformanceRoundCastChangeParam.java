package kr.ac.tukorea.bandi.domain.performance.dto.request;

import kr.ac.tukorea.bandi.domain.performance.model.CastType;

public record PerformanceRoundCastChangeParam(
        Long performanceRoundCastId,
        Long publicProfileId,
        CastType castType,
        String reason
) {
}
