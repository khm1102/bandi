package kr.ac.tukorea.bandi.domain.member.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class SchoolCredentialsInvalidException extends BusinessException {

    public SchoolCredentialsInvalidException() {
        super(ErrorCode.SCHOOL_CREDENTIALS_INVALID);
    }
}
