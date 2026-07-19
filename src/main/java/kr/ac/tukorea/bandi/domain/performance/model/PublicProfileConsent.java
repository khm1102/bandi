package kr.ac.tukorea.bandi.domain.performance.model;

import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPublicProfileConsentException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PublicProfileConsent {

    private final Long publicProfileConsentId;
    private final Long publicProfileId;
    private final Long policyDocumentVersionId;
    private final ConsentScope consentScope;
    private final boolean agreed;
    private final LocalDateTime agreedDttm;
    private final LocalDateTime revokedDttm;
    private final Long recordedByMemberId;

    public PublicProfileConsent(Long publicProfileConsentId,
                                Long publicProfileId,
                                Long policyDocumentVersionId,
                                ConsentScope consentScope, boolean agreed,
                                LocalDateTime agreedDttm,
                                LocalDateTime revokedDttm,
                                Long recordedByMemberId) {
        this.publicProfileConsentId = publicProfileConsentId;
        this.publicProfileId = requireId(publicProfileId, "publicProfileId");
        this.policyDocumentVersionId = requireId(
                policyDocumentVersionId, "policyDocumentVersionId");
        this.consentScope = requireScope(consentScope);
        this.agreed = agreed;
        this.agreedDttm = requireTime(agreedDttm, "agreedDttm");
        this.revokedDttm = revokedDttm;
        this.recordedByMemberId = requireId(
                recordedByMemberId, "recordedByMemberId");
        validateState();
    }

    public static PublicProfileConsent agree(
            Long publicProfileId, Long policyDocumentVersionId,
            ConsentScope consentScope, Long recordedByMemberId,
            LocalDateTime agreedDttm) {
        return new PublicProfileConsent(null, publicProfileId,
                policyDocumentVersionId, consentScope, true, agreedDttm,
                null, recordedByMemberId);
    }

    public PublicProfileConsent revoke(Long recordedByMemberId,
                                       LocalDateTime revokedDttm) {
        if (!agreed || this.revokedDttm != null || revokedDttm == null
                || revokedDttm.isBefore(agreedDttm)) {
            throw new InvalidPublicProfileConsentException("revoke");
        }
        return new PublicProfileConsent(publicProfileConsentId,
                publicProfileId, policyDocumentVersionId, consentScope,
                false, agreedDttm, revokedDttm, recordedByMemberId);
    }

    private void validateState() {
        if (agreed && revokedDttm != null) {
            throw new InvalidPublicProfileConsentException("state");
        }
        if (!agreed && revokedDttm == null) {
            throw new InvalidPublicProfileConsentException("state");
        }
    }

    private static Long requireId(Long value, String field) {
        if (value == null || value < 1) {
            throw new InvalidPublicProfileConsentException(field);
        }
        return value;
    }

    private static ConsentScope requireScope(ConsentScope value) {
        if (value == null) {
            throw new InvalidPublicProfileConsentException("consentScope");
        }
        return value;
    }

    private static LocalDateTime requireTime(LocalDateTime value,
                                             String field) {
        if (value == null) {
            throw new InvalidPublicProfileConsentException(field);
        }
        return value;
    }
}
