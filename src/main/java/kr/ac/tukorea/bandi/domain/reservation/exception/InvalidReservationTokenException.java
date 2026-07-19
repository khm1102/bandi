package kr.ac.tukorea.bandi.domain.reservation.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class InvalidReservationTokenException extends BusinessException {

    public InvalidReservationTokenException() {
        super(ErrorCode.INVALID_RESERVATION_TOKEN);
    }
}
