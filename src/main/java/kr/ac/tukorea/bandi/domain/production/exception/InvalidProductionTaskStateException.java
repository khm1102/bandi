package kr.ac.tukorea.bandi.domain.production.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class InvalidProductionTaskStateException extends BusinessException {

    public InvalidProductionTaskStateException(String detail) {
        super(ErrorCode.INVALID_PRODUCTION_TASK_STATE, detail);
    }
}
