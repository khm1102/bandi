package kr.ac.tukorea.bandi.domain.performance.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class PerformancePublicPageNotFoundException extends BusinessException {

    public PerformancePublicPageNotFoundException(String detail) {
        super(ErrorCode.PERFORMANCE_PUBLIC_PAGE_NOT_FOUND, detail);
    }
}
