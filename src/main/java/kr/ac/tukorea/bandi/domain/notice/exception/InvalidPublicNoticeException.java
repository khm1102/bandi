package kr.ac.tukorea.bandi.domain.notice.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class InvalidPublicNoticeException extends BusinessException {

    public InvalidPublicNoticeException(String field) {
        super(ErrorCode.INVALID_INPUT, field);
    }
}
