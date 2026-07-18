package kr.ac.tukorea.bandi.domain.checklist.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class ChecklistAccessDeniedException extends BusinessException {

    public ChecklistAccessDeniedException() {
        super(ErrorCode.FORBIDDEN);
    }
}
