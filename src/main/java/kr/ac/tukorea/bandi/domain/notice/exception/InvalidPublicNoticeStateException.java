package kr.ac.tukorea.bandi.domain.notice.exception;

import kr.ac.tukorea.bandi.domain.notice.model.PublicNoticeStatus;
import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class InvalidPublicNoticeStateException extends BusinessException {

    public InvalidPublicNoticeStateException(PublicNoticeStatus status) {
        super(ErrorCode.INVALID_PUBLIC_NOTICE_STATE, status.name());
    }
}
