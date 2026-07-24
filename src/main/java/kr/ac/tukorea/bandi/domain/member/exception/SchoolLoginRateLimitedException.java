package kr.ac.tukorea.bandi.domain.member.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class SchoolLoginRateLimitedException extends BusinessException {

    public SchoolLoginRateLimitedException() {
        super(ErrorCode.SCHOOL_LOGIN_RATE_LIMITED);
    }
}
