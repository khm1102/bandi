package kr.ac.tukorea.bandi.domain.policy.dto.request;

import java.time.LocalDateTime;

public record PolicyVersionPublishParam(
        Long policyDocumentId,
        String body,
        LocalDateTime effectiveFromDttm,
        boolean required
) {
}
