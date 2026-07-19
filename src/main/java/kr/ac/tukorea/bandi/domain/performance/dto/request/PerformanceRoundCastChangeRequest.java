package kr.ac.tukorea.bandi.domain.performance.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import kr.ac.tukorea.bandi.domain.performance.model.CastType;

public record PerformanceRoundCastChangeRequest(
        @NotNull @Positive Long publicProfileId,
        @NotNull CastType castType,
        @Size(max = 500) String reason
) {

    public PerformanceRoundCastChangeParam toParam(Long roundCastId) {
        return new PerformanceRoundCastChangeParam(roundCastId,
                publicProfileId, castType, reason);
    }
}
