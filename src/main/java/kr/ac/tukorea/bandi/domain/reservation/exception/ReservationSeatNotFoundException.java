package kr.ac.tukorea.bandi.domain.reservation.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class ReservationSeatNotFoundException extends BusinessException {

    public ReservationSeatNotFoundException(Long reservationSeatId) {
        super(ErrorCode.RESERVATION_SEAT_NOT_FOUND);
    }
}
