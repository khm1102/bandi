package kr.ac.tukorea.bandi.domain.performance.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class InvalidPublicProfileConsentException extends BusinessException {

    public InvalidPublicProfileConsentException(String detail) {
        super(ErrorCode.INVALID_PUBLIC_PROFILE_STATE, detail);
    }
}
