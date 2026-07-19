package kr.ac.tukorea.bandi.domain.policy.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kr.ac.tukorea.bandi.domain.policy.model.PolicyAudience;
import kr.ac.tukorea.bandi.domain.policy.model.PolicyType;

public record PolicyDocumentRequest(
        @NotNull PolicyType policyType,
        @NotBlank @Size(max = 200) String title,
        @NotNull PolicyAudience audience
) {

    public PolicyDocumentCreateParam toParam() {
        return new PolicyDocumentCreateParam(policyType, title, audience);
    }
}
