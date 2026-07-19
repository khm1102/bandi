package kr.ac.tukorea.bandi.domain.activity.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class ActivityRecordNotFoundException extends BusinessException {

    public ActivityRecordNotFoundException(Long activityRecordId) {
        super(ErrorCode.ACTIVITY_RECORD_NOT_FOUND);
    }
}
