package kr.ac.tukorea.bandi.domain.member.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class SchoolIdentityMismatchException extends BusinessException {

    public SchoolIdentityMismatchException() {
        super(ErrorCode.SCHOOL_IDENTITY_MISMATCH);
    }
}
