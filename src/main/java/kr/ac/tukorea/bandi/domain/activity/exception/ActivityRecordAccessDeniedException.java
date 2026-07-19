package kr.ac.tukorea.bandi.domain.activity.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class ActivityRecordAccessDeniedException extends BusinessException {

    public ActivityRecordAccessDeniedException() {
        super(ErrorCode.FORBIDDEN);
    }
}
