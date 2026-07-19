package kr.ac.tukorea.bandi.domain.reservation.dto.request;

import kr.ac.tukorea.bandi.domain.reservation.model.ReservationStatus;

public record ReservationManageFilter(ReservationStatus status) {
}
