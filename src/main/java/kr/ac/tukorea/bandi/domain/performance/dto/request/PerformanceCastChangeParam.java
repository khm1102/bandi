package kr.ac.tukorea.bandi.domain.performance.dto.request;

import kr.ac.tukorea.bandi.domain.performance.model.CastType;

public record PerformanceCastChangeParam(
        Long performanceCastId,
        Long publicProfileId,
        CastType castType,
        int displayOrder,
        String reason
) {
}
