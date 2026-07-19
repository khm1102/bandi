package kr.ac.tukorea.bandi.domain.policy.dto.response;

import kr.ac.tukorea.bandi.domain.policy.model.PolicyAudience;
import kr.ac.tukorea.bandi.domain.policy.model.PolicyType;

import java.time.LocalDateTime;

public record PolicyDocumentResponse(
        Long policyDocumentId,
        PolicyType policyType,
        String title,
        PolicyAudience audience,
        boolean active,
        LocalDateTime createdDttm,
        LocalDateTime updatedDttm
) {
}
