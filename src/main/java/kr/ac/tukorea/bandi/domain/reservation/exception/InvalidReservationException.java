package kr.ac.tukorea.bandi.domain.reservation.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class InvalidReservationException extends BusinessException {

    public InvalidReservationException(String detail) {
        super(ErrorCode.INVALID_RESERVATION);
    }
}
