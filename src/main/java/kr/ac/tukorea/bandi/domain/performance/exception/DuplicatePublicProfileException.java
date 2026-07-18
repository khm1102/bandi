package kr.ac.tukorea.bandi.domain.performance.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class DuplicatePublicProfileException extends BusinessException {

    public DuplicatePublicProfileException(Long memberId) {
        super(ErrorCode.DUPLICATE_PUBLIC_PROFILE, "memberId=" + memberId);
    }
}
