package kr.ac.tukorea.bandi.domain.policy.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record PolicyVersionRequest(
        @NotBlank String body,
        @NotNull LocalDateTime effectiveFromDttm,
        boolean required
) {

    public PolicyVersionPublishParam toParam(Long policyDocumentId) {
        return new PolicyVersionPublishParam(policyDocumentId, body,
                effectiveFromDttm, required);
    }
}
