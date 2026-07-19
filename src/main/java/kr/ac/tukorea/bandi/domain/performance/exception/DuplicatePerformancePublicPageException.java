package kr.ac.tukorea.bandi.domain.performance.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class DuplicatePerformancePublicPageException extends BusinessException {

    public DuplicatePerformancePublicPageException(String slug) {
        super(ErrorCode.DUPLICATE_PERFORMANCE_PUBLIC_PAGE,
                "slug=" + slug);
    }
}
