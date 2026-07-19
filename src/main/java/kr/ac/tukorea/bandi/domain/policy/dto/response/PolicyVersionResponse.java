package kr.ac.tukorea.bandi.domain.policy.dto.response;

import java.time.LocalDateTime;

public record PolicyVersionResponse(
        Long policyDocumentVersionId,
        Long policyDocumentId,
        int versionNo,
        String body,
        LocalDateTime publishedDttm,
        Long publishedByMemberId,
        LocalDateTime effectiveFromDttm,
        boolean required
) {
}
