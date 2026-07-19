package kr.ac.tukorea.bandi.domain.reservation.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class ReservationNotFoundException extends BusinessException {

    public ReservationNotFoundException(Long reservationId) {
        super(ErrorCode.RESERVATION_NOT_FOUND);
    }
}
