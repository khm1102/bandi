package kr.ac.tukorea.bandi.domain.notice.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class PublicNoticeAccessDeniedException extends BusinessException {

    public PublicNoticeAccessDeniedException() {
        super(ErrorCode.FORBIDDEN);
    }
}
