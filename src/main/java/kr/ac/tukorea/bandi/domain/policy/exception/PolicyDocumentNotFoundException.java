package kr.ac.tukorea.bandi.domain.policy.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class PolicyDocumentNotFoundException extends BusinessException {

    public PolicyDocumentNotFoundException(Long policyDocumentId) {
        super(ErrorCode.POLICY_DOCUMENT_NOT_FOUND,
                "policyDocumentId=" + policyDocumentId);
    }
}
