package kr.ac.tukorea.bandi.domain.policy.dto.request;

import kr.ac.tukorea.bandi.domain.policy.model.PolicyAudience;
import kr.ac.tukorea.bandi.domain.policy.model.PolicyType;

public record PolicyDocumentCreateParam(
        PolicyType policyType,
        String title,
        PolicyAudience audience
) {
}
