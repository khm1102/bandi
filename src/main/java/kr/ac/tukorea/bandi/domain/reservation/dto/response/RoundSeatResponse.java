package kr.ac.tukorea.bandi.domain.reservation.dto.response;

import kr.ac.tukorea.bandi.domain.reservation.model.PerformanceRoundSeat;
import kr.ac.tukorea.bandi.domain.reservation.model.RoundSeatStatus;

public record RoundSeatResponse(
        Long performanceRoundSeatId,
        Long performanceRoundId,
        String seatLabel,
        String sectionCode,
        String rowLabel,
        String columnLabel,
        Integer displayRow,
        Integer displayColumn,
        RoundSeatStatus status,
        String accessibilityCode
) {

    public static RoundSeatResponse from(PerformanceRoundSeat seat) {
        return new RoundSeatResponse(seat.getPerformanceRoundSeatId(),
                seat.getPerformanceRoundId(), seat.getSeatLabel(),
                seat.getSectionCode(), seat.getRowLabel(),
                seat.getColumnLabel(), seat.getDisplayRow(),
                seat.getDisplayColumn(), seat.getStatus(),
                seat.getAccessibilityCode());
    }
}
