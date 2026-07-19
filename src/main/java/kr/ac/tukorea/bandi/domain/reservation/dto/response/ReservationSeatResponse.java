package kr.ac.tukorea.bandi.domain.reservation.dto.response;

import kr.ac.tukorea.bandi.domain.reservation.model.ReservationSeatStatus;

import java.time.LocalDateTime;

public record ReservationSeatResponse(
        Long reservationSeatId,
        Long performanceRoundSeatId,
        String seatLabel,
        String sectionCode,
        String rowLabel,
        String columnLabel,
        ReservationSeatStatus status,
        LocalDateTime checkedInDttm,
        Long checkedInByMemberId
) {

    public boolean checkedIn() {
        return checkedInDttm != null;
    }
}
