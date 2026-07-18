package kr.ac.tukorea.bandi.domain.performance.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class DuplicatePerformanceTermException extends BusinessException {

    public DuplicatePerformanceTermException(short academicYear,
                                             String termCode) {
        super(ErrorCode.DUPLICATE_PERFORMANCE_TERM,
                "%s:%s".formatted(academicYear, termCode));
    }
}
