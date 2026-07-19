package kr.ac.tukorea.bandi.domain.resource.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class InvalidResourceException extends BusinessException {

    public InvalidResourceException(String field) {
        super(ErrorCode.INVALID_INPUT);
    }
}
