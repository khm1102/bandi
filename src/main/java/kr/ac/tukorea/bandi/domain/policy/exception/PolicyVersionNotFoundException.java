package kr.ac.tukorea.bandi.domain.policy.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class PolicyVersionNotFoundException extends BusinessException {

    public PolicyVersionNotFoundException(String context) {
        super(ErrorCode.POLICY_VERSION_NOT_FOUND, context);
    }
}
