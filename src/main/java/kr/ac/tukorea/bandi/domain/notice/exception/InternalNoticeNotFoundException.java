package kr.ac.tukorea.bandi.domain.notice.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class InternalNoticeNotFoundException extends BusinessException {

    public InternalNoticeNotFoundException(Long internalNoticeId) {
        super(ErrorCode.INTERNAL_NOTICE_NOT_FOUND, String.valueOf(internalNoticeId));
    }
}
