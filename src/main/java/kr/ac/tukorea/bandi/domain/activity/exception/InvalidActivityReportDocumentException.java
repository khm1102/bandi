package kr.ac.tukorea.bandi.domain.activity.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class InvalidActivityReportDocumentException extends BusinessException {

    public InvalidActivityReportDocumentException(String field) {
        super(ErrorCode.INVALID_ACTIVITY_REPORT_DOCUMENT, field);
    }
}
