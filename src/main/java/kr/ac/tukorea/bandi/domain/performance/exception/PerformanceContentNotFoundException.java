package kr.ac.tukorea.bandi.domain.performance.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class PerformanceContentNotFoundException extends BusinessException {

    public PerformanceContentNotFoundException(String detail) {
        super(ErrorCode.PERFORMANCE_CONTENT_NOT_FOUND, detail);
    }
}
