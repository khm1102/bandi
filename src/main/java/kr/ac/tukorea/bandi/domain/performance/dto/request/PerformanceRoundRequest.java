package kr.ac.tukorea.bandi.domain.performance.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record PerformanceRoundRequest(
        @NotNull @Positive Long performanceProjectId,
        @Positive int roundNo,
        @NotNull LocalDateTime startDttm,
        @NotNull LocalDateTime entryStartDttm,
        @NotNull LocalDateTime reservationOpenDttm,
        @NotNull LocalDateTime reservationCloseDttm
) {

    public PerformanceRoundWriteParam toParam(Long performanceRoundId) {
        return new PerformanceRoundWriteParam(performanceRoundId,
                performanceProjectId, roundNo, startDttm, entryStartDttm,
                reservationOpenDttm, reservationCloseDttm);
    }
}
