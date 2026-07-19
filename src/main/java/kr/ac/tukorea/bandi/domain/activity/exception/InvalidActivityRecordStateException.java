package kr.ac.tukorea.bandi.domain.activity.exception;

import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecordStatus;
import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class InvalidActivityRecordStateException extends BusinessException {

    public InvalidActivityRecordStateException(ActivityRecordStatus status) {
        super(ErrorCode.INVALID_ACTIVITY_RECORD_STATE);
    }

    public InvalidActivityRecordStateException(String state) {
        super(ErrorCode.INVALID_ACTIVITY_RECORD_STATE);
    }
}
