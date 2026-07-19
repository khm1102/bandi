package kr.ac.tukorea.bandi.domain.reservation.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class DuplicateRoundSeatException extends BusinessException {

    public DuplicateRoundSeatException(Long performanceRoundId,
                                       String seatLabel) {
        super(ErrorCode.DUPLICATE_ROUND_SEAT);
    }
}
