package kr.ac.tukorea.bandi.domain.performance.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class DuplicatePerformanceContentException extends BusinessException {

    public DuplicatePerformanceContentException(String detail) {
        super(ErrorCode.DUPLICATE_PERFORMANCE_CONTENT, detail);
    }
}
