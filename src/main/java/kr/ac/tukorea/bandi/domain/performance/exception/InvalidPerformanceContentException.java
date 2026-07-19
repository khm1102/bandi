package kr.ac.tukorea.bandi.domain.performance.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class InvalidPerformanceContentException extends BusinessException {

    public InvalidPerformanceContentException(String detail) {
        super(ErrorCode.INVALID_INPUT, detail);
    }
}
