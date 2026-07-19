package kr.ac.tukorea.bandi.domain.reservation.service;

public record ReservationCredentials(
        String reservationNo,
        String lookupToken,
        String lookupTokenHash,
        String entryToken,
        String entryTokenHash
) {
}
