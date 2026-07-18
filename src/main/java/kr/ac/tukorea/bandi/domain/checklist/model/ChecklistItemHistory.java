package kr.ac.tukorea.bandi.domain.checklist.model;

import kr.ac.tukorea.bandi.domain.checklist.exception.InvalidChecklistItemException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ChecklistItemHistory {

    private Long checklistItemHistoryId;
    private final Long checklistItemId;
    private final boolean previousCompleted;
    private final boolean newCompleted;
    private final Long changedByMemberId;
    private final LocalDateTime changedDttm;
    private final String reason;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;

    public ChecklistItemHistory(
            Long checklistItemHistoryId, Long checklistItemId,
            boolean previousCompleted, boolean newCompleted,
            Long changedByMemberId, LocalDateTime changedDttm,
            String reason, LocalDateTime createdDttm,
            LocalDateTime updatedDttm) {
        this.checklistItemHistoryId = checklistItemHistoryId;
        this.checklistItemId = requireId(checklistItemId,
                "checklistItemId");
        this.previousCompleted = previousCompleted;
        this.newCompleted = newCompleted;
        this.changedByMemberId = requireId(changedByMemberId,
                "changedByMemberId");
        this.changedDttm = requireTime(changedDttm);
        this.reason = optionalReason(reason);
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
        if (previousCompleted == newCompleted) {
            throw new InvalidChecklistItemException("noChange");
        }
    }

    public static ChecklistItemHistory change(
            Long checklistItemId, boolean previousCompleted,
            boolean newCompleted, Long changedByMemberId,
            LocalDateTime changedDttm, String reason) {
        return new ChecklistItemHistory(null, checklistItemId,
                previousCompleted, newCompleted, changedByMemberId,
                changedDttm, reason, null, null);
    }

    private static Long requireId(Long value, String field) {
        if (value == null || value < 1) {
            throw new InvalidChecklistItemException(field);
        }
        return value;
    }

    private static LocalDateTime requireTime(LocalDateTime value) {
        if (value == null) {
            throw new InvalidChecklistItemException("changedDttm");
        }
        return value;
    }

    private static String optionalReason(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (value.strip().length() > 500) {
            throw new InvalidChecklistItemException("reason");
        }
        return value.strip();
    }
}
