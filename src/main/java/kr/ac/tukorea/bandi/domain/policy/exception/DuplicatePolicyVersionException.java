package kr.ac.tukorea.bandi.domain.policy.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class DuplicatePolicyVersionException extends BusinessException {

    public DuplicatePolicyVersionException(Long policyDocumentId) {
        super(ErrorCode.DUPLICATE_POLICY_VERSION,
                "policyDocumentId=" + policyDocumentId);
    }
}
