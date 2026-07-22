package kr.ac.tukorea.bandi.domain.notice.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class PublicNoticeNotFoundException extends BusinessException {

    public PublicNoticeNotFoundException(Long publicNoticeId) {
        super(ErrorCode.PUBLIC_NOTICE_NOT_FOUND, String.valueOf(publicNoticeId));
    }
}
