package kr.ac.tukorea.bandi.domain.member.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class SchoolSsoResponseChangedException extends BusinessException {

    public SchoolSsoResponseChangedException() {
        super(ErrorCode.SCHOOL_SSO_RESPONSE_CHANGED);
    }
}
