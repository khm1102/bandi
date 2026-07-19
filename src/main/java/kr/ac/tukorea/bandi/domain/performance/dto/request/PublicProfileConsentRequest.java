package kr.ac.tukorea.bandi.domain.performance.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import kr.ac.tukorea.bandi.domain.performance.model.ConsentScope;

public record PublicProfileConsentRequest(
        @NotNull @Positive Long policyDocumentVersionId,
        @NotNull ConsentScope consentScope
) {
}
