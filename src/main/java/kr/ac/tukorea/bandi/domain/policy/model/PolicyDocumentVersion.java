package kr.ac.tukorea.bandi.domain.policy.model;

import kr.ac.tukorea.bandi.domain.policy.exception.InvalidPolicyDocumentException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PolicyDocumentVersion {

    private final Long policyDocumentVersionId;
    private final Long policyDocumentId;
    private final int versionNo;
    private final String body;
    private final LocalDateTime publishedDttm;
    private final Long publishedByMemberId;
    private final LocalDateTime effectiveFromDttm;
    private final boolean required;

    public PolicyDocumentVersion(Long policyDocumentVersionId,
                                 Long policyDocumentId, Integer versionNo,
                                 String body, LocalDateTime publishedDttm,
                                 Long publishedByMemberId,
                                 LocalDateTime effectiveFromDttm,
                                 boolean required) {
        this.policyDocumentVersionId = policyDocumentVersionId;
        this.policyDocumentId = requirePositive(policyDocumentId, "policyDocumentId");
        this.versionNo = requireVersion(versionNo);
        this.body = requireBody(body);
        this.publishedDttm = requireTime(publishedDttm, "publishedDttm");
        this.publishedByMemberId = publishedByMemberId;
        this.effectiveFromDttm = requireTime(effectiveFromDttm, "effectiveFromDttm");
        if (effectiveFromDttm.isBefore(publishedDttm)) {
            throw new InvalidPolicyDocumentException("effectiveFromDttm");
        }
        this.required = required;
    }

    public static PolicyDocumentVersion publish(
            Long policyDocumentId, int versionNo, String body,
            LocalDateTime publishedDttm, LocalDateTime effectiveFromDttm,
            boolean required, Long publishedByMemberId) {
        return new PolicyDocumentVersion(null, policyDocumentId, versionNo,
                body, publishedDttm, publishedByMemberId,
                effectiveFromDttm, required);
    }

    public boolean isEffectiveAt(LocalDateTime moment) {
        return moment != null && !moment.isBefore(effectiveFromDttm);
    }

    private static Long requirePositive(Long value, String field) {
        if (value == null || value < 1) {
            throw new InvalidPolicyDocumentException(field);
        }
        return value;
    }

    private static int requireVersion(Integer value) {
        if (value == null || value < 1) {
            throw new InvalidPolicyDocumentException("versionNo");
        }
        return value;
    }

    private static String requireBody(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidPolicyDocumentException("body");
        }
        return value;
    }

    private static LocalDateTime requireTime(LocalDateTime value,
                                             String field) {
        if (value == null) {
            throw new InvalidPolicyDocumentException(field);
        }
        return value;
    }
}
