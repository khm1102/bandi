package kr.ac.tukorea.bandi.domain.audit.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class InvalidAuditLogException extends BusinessException {

    public InvalidAuditLogException(String detail) {
        super(ErrorCode.INVALID_AUDIT_LOG, detail);
    }
}
