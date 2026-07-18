package kr.ac.tukorea.bandi.domain.performance.dto.response;

import kr.ac.tukorea.bandi.domain.performance.model.PerformanceRoundStatus;

import java.time.LocalDateTime;

public record PerformanceRoundResponse(
        Long performanceRoundId,
        Long performanceProjectId,
        int roundNo,
        LocalDateTime startDttm,
        LocalDateTime entryStartDttm,
        LocalDateTime reservationOpenDttm,
        LocalDateTime reservationCloseDttm,
        PerformanceRoundStatus status
) {
}
