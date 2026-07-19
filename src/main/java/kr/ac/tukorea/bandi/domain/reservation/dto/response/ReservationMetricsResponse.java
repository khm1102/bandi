package kr.ac.tukorea.bandi.domain.reservation.dto.response;

import java.math.BigDecimal;

public record ReservationMetricsResponse(
        long reservationCount,
        long reservedSeatCount,
        long checkedInSeatCount,
        long notCheckedInSeatCount,
        long partiallyCheckedInReservationCount,
        BigDecimal entryRate
) {
}
