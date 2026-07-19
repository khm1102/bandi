package kr.ac.tukorea.bandi.domain.reservation.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class ReservationSecurityException extends BusinessException {

    public ReservationSecurityException(Throwable cause) {
        super(ErrorCode.RESERVATION_SECURITY_FAILURE);
        initCause(cause);
    }
}
