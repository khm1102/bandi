package kr.ac.tukorea.bandi.domain.performance.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import kr.ac.tukorea.bandi.domain.performance.model.CastType;

public record PerformanceCastChangeRequest(
        @NotNull @Positive Long publicProfileId,
        @NotNull CastType castType,
        @PositiveOrZero int displayOrder,
        @Size(max = 500) String reason
) {

    public PerformanceCastChangeParam toParam(Long castId) {
        return new PerformanceCastChangeParam(castId, publicProfileId,
                castType, displayOrder, reason);
    }
}
