package kr.ac.tukorea.bandi.domain.performance.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class PerformanceAccessDeniedException extends BusinessException {

    public PerformanceAccessDeniedException() {
        super(ErrorCode.FORBIDDEN);
    }
}
