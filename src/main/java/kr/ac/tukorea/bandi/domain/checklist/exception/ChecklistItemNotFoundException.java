package kr.ac.tukorea.bandi.domain.checklist.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class ChecklistItemNotFoundException extends BusinessException {

    public ChecklistItemNotFoundException(Long checklistItemId) {
        super(ErrorCode.CHECKLIST_ITEM_NOT_FOUND);
    }
}
