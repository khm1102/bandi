package kr.ac.tukorea.bandi.domain.performance.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class InvalidPerformanceProjectStateException extends BusinessException {

    public InvalidPerformanceProjectStateException(String detail) {
        super(ErrorCode.INVALID_PERFORMANCE_PROJECT_STATE, detail);
    }
}
