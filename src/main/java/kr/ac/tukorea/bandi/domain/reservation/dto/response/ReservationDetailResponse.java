package kr.ac.tukorea.bandi.domain.reservation.dto.response;

import kr.ac.tukorea.bandi.domain.reservation.model.ReservationStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ReservationDetailResponse(
        Long reservationId,
        Long performanceRoundId,
        String reservationNo,
        String applicantName,
        String phone,
        ReservationStatus status,
        Long privacyPolicyVersionId,
        LocalDateTime agreedDttm,
        LocalDateTime cancelledDttm,
        String cancelReason,
        boolean cancelable,
        List<ReservationSeatResponse> seats
) {

    public ReservationDetailResponse {
        seats = List.copyOf(seats);
    }
}
