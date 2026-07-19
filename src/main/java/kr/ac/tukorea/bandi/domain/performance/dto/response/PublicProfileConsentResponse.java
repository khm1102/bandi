package kr.ac.tukorea.bandi.domain.performance.dto.response;

import kr.ac.tukorea.bandi.domain.performance.model.ConsentScope;

import java.time.LocalDateTime;

public record PublicProfileConsentResponse(
        Long publicProfileConsentId,
        Long publicProfileId,
        Long policyDocumentVersionId,
        ConsentScope consentScope,
        boolean agreed,
        LocalDateTime agreedDttm,
        LocalDateTime revokedDttm,
        Long recordedByMemberId
) {
}
