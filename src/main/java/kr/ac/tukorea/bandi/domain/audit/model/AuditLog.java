package kr.ac.tukorea.bandi.domain.audit.model;

import kr.ac.tukorea.bandi.domain.audit.exception.InvalidAuditLogException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AuditLog {

    private Long auditLogId;
    private final Long actorMemberId;
    private final AuditAction action;
    private final AuditTargetType targetType;
    private final Long targetId;
    private final String summary;
    private final String metadataJson;
    private final LocalDateTime occurredDttm;

    public AuditLog(Long auditLogId, Long actorMemberId,
                    AuditAction action, AuditTargetType targetType,
                    Long targetId, String summary, String metadataJson,
                    LocalDateTime occurredDttm) {
        this.auditLogId = auditLogId;
        this.actorMemberId = actorMemberId;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.summary = summary;
        this.metadataJson = metadataJson;
        this.occurredDttm = occurredDttm;
    }

    public static AuditLog record(Long actorMemberId, AuditAction action,
                                  AuditTargetType targetType, Long targetId,
                                  String summary, LocalDateTime occurredDttm) {
        if (actorMemberId == null || action == null || targetType == null
                || targetId == null || summary == null || summary.isBlank()
                || summary.strip().length() > 500
                || occurredDttm == null) {
            throw new InvalidAuditLogException("required");
        }
        return new AuditLog(null, actorMemberId, action, targetType,
                targetId, summary.strip(), null, occurredDttm);
    }
}
