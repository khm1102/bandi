package kr.ac.tukorea.bandi.domain.activity.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class InvalidActivityRecordException extends BusinessException {

    public InvalidActivityRecordException(String field) {
        super(ErrorCode.INVALID_INPUT);
    }
}
