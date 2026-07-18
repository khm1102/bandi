package kr.ac.tukorea.bandi.domain.checklist.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class InvalidChecklistItemException extends BusinessException {

    public InvalidChecklistItemException(String detail) {
        super(ErrorCode.INVALID_INPUT);
    }
}
