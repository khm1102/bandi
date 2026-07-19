package kr.ac.tukorea.bandi.domain.reservation.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class SeatUnavailableException extends BusinessException {

    public SeatUnavailableException(Long performanceRoundSeatId) {
        super(ErrorCode.SEAT_UNAVAILABLE);
    }
}
