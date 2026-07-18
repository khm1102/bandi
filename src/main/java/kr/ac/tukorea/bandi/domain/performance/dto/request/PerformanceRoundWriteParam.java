package kr.ac.tukorea.bandi.domain.performance.dto.request;

import java.time.LocalDateTime;

public record PerformanceRoundWriteParam(
        Long performanceRoundId,
        Long performanceProjectId,
        int roundNo,
        LocalDateTime startDttm,
        LocalDateTime entryStartDttm,
        LocalDateTime reservationOpenDttm,
        LocalDateTime reservationCloseDttm
) {
}
