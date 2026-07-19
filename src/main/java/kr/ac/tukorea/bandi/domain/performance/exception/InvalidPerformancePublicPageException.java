package kr.ac.tukorea.bandi.domain.performance.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class InvalidPerformancePublicPageException extends BusinessException {

    public InvalidPerformancePublicPageException(String detail) {
        super(ErrorCode.INVALID_INPUT, detail);
    }
}
