package kr.ac.tukorea.bandi.domain.file.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class InvalidFileScopeException extends BusinessException {

    public InvalidFileScopeException() {
        super(ErrorCode.INVALID_FILE_SCOPE);
    }
}
