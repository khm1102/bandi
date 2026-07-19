package kr.ac.tukorea.bandi.domain.policy.model;

import kr.ac.tukorea.bandi.domain.policy.exception.InvalidPolicyDocumentException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PolicyDocument {

    private final Long policyDocumentId;
    private final PolicyType policyType;
    private final String title;
    private final PolicyAudience audience;
    private final boolean active;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;

    public PolicyDocument(Long policyDocumentId, PolicyType policyType,
                          String title, PolicyAudience audience, boolean active,
                          LocalDateTime createdDttm, LocalDateTime updatedDttm) {
        this.policyDocumentId = policyDocumentId;
        this.policyType = requireValue(policyType, "policyType");
        this.title = requireText(title, 200, "title");
        this.audience = requireValue(audience, "audience");
        this.active = active;
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
    }

    public static PolicyDocument create(PolicyType policyType, String title,
                                        PolicyAudience audience) {
        return new PolicyDocument(null, policyType, title, audience,
                true, null, null);
    }

    public PolicyDocument changeActive(boolean active) {
        if (this.active == active) {
            throw new InvalidPolicyDocumentException("active");
        }
        return new PolicyDocument(policyDocumentId, policyType, title,
                audience, active, createdDttm, updatedDttm);
    }

    private static String requireText(String value, int maxLength,
                                      String field) {
        if (value == null || value.isBlank()
                || value.strip().length() > maxLength) {
            throw new InvalidPolicyDocumentException(field);
        }
        return value.strip();
    }

    private static <T> T requireValue(T value, String field) {
        if (value == null) {
            throw new InvalidPolicyDocumentException(field);
        }
        return value;
    }
}
