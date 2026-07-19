package kr.ac.tukorea.bandi.domain.reservation.dto.response;

public record ReservationCreatedResponse(
        Long reservationId,
        String reservationNo,
        String lookupToken,
        String entryToken
) {
}
