package kr.ac.tukorea.bandi.domain.notice.exception;

import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeStatus;
import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class InvalidInternalNoticeStateException extends BusinessException {

    public InvalidInternalNoticeStateException(InternalNoticeStatus status) {
        super(ErrorCode.INVALID_INTERNAL_NOTICE_STATE, status.name());
    }
}
