package kr.ac.tukorea.bandi.domain.policy.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class InvalidPolicyVersionException extends BusinessException {

    public InvalidPolicyVersionException(String detail) {
        super(ErrorCode.INVALID_POLICY_VERSION, detail);
    }
}
