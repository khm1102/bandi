package kr.ac.tukorea.bandi.domain.performance.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import kr.ac.tukorea.bandi.domain.performance.model.CastType;

public record PerformanceRoundCastAssignRequest(
        @NotNull @Positive Long performanceProjectId,
        @NotNull @Positive Long performanceRoundId,
        @NotNull @Positive Long performanceCharacterId,
        @NotNull @Positive Long publicProfileId,
        @NotNull CastType castType,
        @Size(max = 500) String reason
) {

    public PerformanceRoundCastAssignParam toParam() {
        return new PerformanceRoundCastAssignParam(performanceProjectId,
                performanceRoundId, performanceCharacterId, publicProfileId,
                castType, reason);
    }
}
