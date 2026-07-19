package kr.ac.tukorea.bandi.domain.performance.dto.request;

import kr.ac.tukorea.bandi.domain.performance.model.ConsentScope;

public record PublicProfileConsentParam(
        Long publicProfileId,
        Long policyDocumentVersionId,
        ConsentScope consentScope
) {
}
