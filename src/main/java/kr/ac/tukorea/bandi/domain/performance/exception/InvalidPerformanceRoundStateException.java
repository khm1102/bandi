package kr.ac.tukorea.bandi.domain.performance.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class InvalidPerformanceRoundStateException extends BusinessException {

    public InvalidPerformanceRoundStateException(String detail) {
        super(ErrorCode.INVALID_PERFORMANCE_ROUND_STATE, detail);
    }
}
