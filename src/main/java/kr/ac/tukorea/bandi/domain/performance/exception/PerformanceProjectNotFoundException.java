package kr.ac.tukorea.bandi.domain.performance.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class PerformanceProjectNotFoundException extends BusinessException {

    public PerformanceProjectNotFoundException(Long performanceProjectId) {
        super(ErrorCode.PERFORMANCE_PROJECT_NOT_FOUND,
                String.valueOf(performanceProjectId));
    }
}
