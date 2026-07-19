package kr.ac.tukorea.bandi.domain.policy.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class PolicyAccessDeniedException extends BusinessException {

    public PolicyAccessDeniedException() {
        super(ErrorCode.FORBIDDEN);
    }
}
