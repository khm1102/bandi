package kr.ac.tukorea.bandi.domain.performance.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import kr.ac.tukorea.bandi.domain.performance.model.CastType;

public record PerformanceCastAssignRequest(
        @NotNull @Positive Long performanceProjectId,
        @NotNull @Positive Long performanceCharacterId,
        @NotNull @Positive Long publicProfileId,
        @NotNull CastType castType,
        @PositiveOrZero int displayOrder,
        @Size(max = 500) String reason
) {

    public PerformanceCastAssignParam toParam() {
        return new PerformanceCastAssignParam(performanceProjectId,
                performanceCharacterId, publicProfileId, castType,
                displayOrder, reason);
    }
}
