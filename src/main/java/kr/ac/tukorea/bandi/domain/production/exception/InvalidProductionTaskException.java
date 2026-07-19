package kr.ac.tukorea.bandi.domain.production.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class InvalidProductionTaskException extends BusinessException {

    public InvalidProductionTaskException(String detail) {
        super(ErrorCode.INVALID_INPUT, detail);
    }
}
