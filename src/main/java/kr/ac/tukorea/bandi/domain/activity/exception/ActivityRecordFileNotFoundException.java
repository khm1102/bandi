package kr.ac.tukorea.bandi.domain.activity.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class ActivityRecordFileNotFoundException extends BusinessException {

    public ActivityRecordFileNotFoundException(Long activityRecordFileId) {
        super(ErrorCode.ACTIVITY_RECORD_FILE_NOT_FOUND);
    }
}
