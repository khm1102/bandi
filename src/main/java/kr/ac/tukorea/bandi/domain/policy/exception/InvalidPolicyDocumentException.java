package kr.ac.tukorea.bandi.domain.policy.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class InvalidPolicyDocumentException extends BusinessException {

    public InvalidPolicyDocumentException(String detail) {
        super(ErrorCode.INVALID_INPUT, detail);
    }
}
