package kr.ac.tukorea.bandi.domain.performance.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class InvalidPerformanceViewingGuideException extends BusinessException {

    public InvalidPerformanceViewingGuideException(String detail) {
        super(ErrorCode.INVALID_INPUT, detail);
    }
}
