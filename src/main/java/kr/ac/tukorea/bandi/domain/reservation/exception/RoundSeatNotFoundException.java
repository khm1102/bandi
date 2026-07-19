package kr.ac.tukorea.bandi.domain.reservation.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class RoundSeatNotFoundException extends BusinessException {

    public RoundSeatNotFoundException(Long performanceRoundSeatId) {
        super(ErrorCode.ROUND_SEAT_NOT_FOUND);
    }
}
