package kr.ac.tukorea.bandi.domain.performance.dto.response;

import kr.ac.tukorea.bandi.domain.performance.model.PerformanceRoundStatus;

import java.time.LocalDateTime;
import java.util.List;

public record PublicPerformanceRoundResponse(
        Long performanceRoundId,
        int roundNo,
        LocalDateTime startDttm,
        LocalDateTime entryStartDttm,
        LocalDateTime reservationOpenDttm,
        LocalDateTime reservationCloseDttm,
        PerformanceRoundStatus status,
        List<PerformanceRoundAccessibilityResponse> accessibilities
) {

    public static PublicPerformanceRoundResponse from(
            PerformanceRoundResponse round,
            List<PerformanceRoundAccessibilityResponse> accessibilities) {
        return new PublicPerformanceRoundResponse(
                round.performanceRoundId(), round.roundNo(),
                round.startDttm(), round.entryStartDttm(),
                round.reservationOpenDttm(),
                round.reservationCloseDttm(), round.status(),
                List.copyOf(accessibilities));
    }
}
