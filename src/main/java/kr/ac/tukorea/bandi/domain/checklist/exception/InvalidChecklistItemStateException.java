package kr.ac.tukorea.bandi.domain.checklist.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class InvalidChecklistItemStateException extends BusinessException {

    public InvalidChecklistItemStateException(String detail) {
        super(ErrorCode.INVALID_CHECKLIST_ITEM_STATE);
    }
}
