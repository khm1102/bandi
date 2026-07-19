package kr.ac.tukorea.bandi.domain.reservation.dto.response;

import kr.ac.tukorea.bandi.domain.reservation.model.ReservationStatus;

import java.time.LocalDateTime;
import java.util.List;

public record PublicReservationDetailResponse(
        Long reservationId,
        Long performanceProjectId,
        Long performanceRoundId,
        String performanceTitle,
        String performanceSlug,
        String place,
        int roundNo,
        LocalDateTime startDttm,
        LocalDateTime entryStartDttm,
        String reservationNo,
        String applicantName,
        String phone,
        ReservationStatus status,
        LocalDateTime cancelledDttm,
        String cancelReason,
        boolean cancelable,
        List<ReservationSeatResponse> seats
) {

    public PublicReservationDetailResponse {
        seats = List.copyOf(seats);
    }

    public static PublicReservationDetailResponse from(
            PublicReservationContextResponse context,
            ReservationDetailResponse reservation) {
        return new PublicReservationDetailResponse(
                reservation.reservationId(),
                context.performanceProjectId(),
                reservation.performanceRoundId(),
                context.performanceTitle(), context.performanceSlug(),
                context.place(), context.roundNo(), context.startDttm(),
                context.entryStartDttm(), reservation.reservationNo(),
                reservation.applicantName(), reservation.phone(),
                reservation.status(), reservation.cancelledDttm(),
                reservation.cancelReason(), reservation.cancelable(),
                reservation.seats());
    }
}
