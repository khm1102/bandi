package kr.ac.tukorea.bandi.domain.member.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class SchoolSsoUnavailableException extends BusinessException {

    public SchoolSsoUnavailableException() {
        super(ErrorCode.SCHOOL_SSO_UNAVAILABLE);
    }
}
