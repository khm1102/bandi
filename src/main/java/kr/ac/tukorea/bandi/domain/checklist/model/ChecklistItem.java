package kr.ac.tukorea.bandi.domain.checklist.model;

import kr.ac.tukorea.bandi.domain.checklist.exception.InvalidChecklistItemException;
import kr.ac.tukorea.bandi.domain.checklist.exception.InvalidChecklistItemStateException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ChecklistItem {

    private static final int MAX_CONTENT_LENGTH = 500;

    private Long checklistItemId;
    private final Long performanceProjectId;
    private final Long performanceRoundId;
    private final Long teamId;
    private final ChecklistScope scope;
    private final String content;
    private final boolean required;
    private final int displayOrder;
    private final boolean completed;
    private final Long completedByMemberId;
    private final LocalDateTime completedDttm;
    private final Long createdByMemberId;
    private final Long updatedByMemberId;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;
    private final LocalDateTime deletedDttm;

    public ChecklistItem(
            Long checklistItemId, Long performanceProjectId,
            Long performanceRoundId, Long teamId, ChecklistScope scope,
            String content, boolean required, Integer displayOrder,
            boolean completed, Long completedByMemberId,
            LocalDateTime completedDttm, Long createdByMemberId,
            Long updatedByMemberId, LocalDateTime createdDttm,
            LocalDateTime updatedDttm, LocalDateTime deletedDttm) {
        this.checklistItemId = checklistItemId;
        this.performanceProjectId = requireId(
                performanceProjectId, "performanceProjectId");
        this.performanceRoundId = performanceRoundId;
        this.teamId = requireId(teamId, "teamId");
        this.scope = requireScope(scope);
        this.content = requireContent(content);
        this.required = required;
        this.displayOrder = requireOrder(displayOrder);
        this.completed = completed;
        this.completedByMemberId = completedByMemberId;
        this.completedDttm = completedDttm;
        this.createdByMemberId = requireId(
                createdByMemberId, "createdByMemberId");
        this.updatedByMemberId = requireId(
                updatedByMemberId, "updatedByMemberId");
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
        this.deletedDttm = deletedDttm;
        validateScope();
        validateCompleted();
    }

    public static ChecklistItem project(
            Long performanceProjectId, Long teamId, String content,
            boolean required, int displayOrder, Long actorMemberId) {
        return create(performanceProjectId, null, teamId,
                ChecklistScope.PROJECT, content, required,
                displayOrder, actorMemberId);
    }

    public static ChecklistItem round(
            Long performanceProjectId, Long performanceRoundId,
            Long teamId, String content, boolean required,
            int displayOrder, Long actorMemberId) {
        return create(performanceProjectId, performanceRoundId, teamId,
                ChecklistScope.ROUND, content, required,
                displayOrder, actorMemberId);
    }

    public ChecklistItem edit(String content, boolean required,
                              int displayOrder, Long actorMemberId) {
        return copy(content, required, displayOrder, completed,
                completedByMemberId, completedDttm, actorMemberId);
    }

    public ChecklistItem changeCompleted(
            boolean completed, Long actorMemberId,
            LocalDateTime changedDttm) {
        if (this.completed == completed) {
            throw new InvalidChecklistItemStateException("noChange");
        }
        Long completedBy = completed
                ? requireId(actorMemberId, "completedByMemberId") : null;
        LocalDateTime completedAt = completed
                ? requireTime(changedDttm) : null;
        return copy(content, required, displayOrder, completed,
                completedBy, completedAt, actorMemberId);
    }

    public static ChecklistItem create(
            Long performanceProjectId, Long performanceRoundId,
            Long teamId, ChecklistScope scope, String content,
            boolean required, int displayOrder, Long actorMemberId) {
        return new ChecklistItem(null, performanceProjectId,
                performanceRoundId, teamId, scope, content, required,
                displayOrder, false, null, null, actorMemberId,
                actorMemberId, null, null, null);
    }

    private ChecklistItem copy(
            String content, boolean required, int displayOrder,
            boolean completed, Long completedByMemberId,
            LocalDateTime completedDttm, Long actorMemberId) {
        return new ChecklistItem(checklistItemId, performanceProjectId,
                performanceRoundId, teamId, scope, content, required,
                displayOrder, completed, completedByMemberId,
                completedDttm, createdByMemberId, actorMemberId,
                createdDttm, updatedDttm, deletedDttm);
    }

    private void validateScope() {
        if (scope == ChecklistScope.PROJECT
                && performanceRoundId != null) {
            throw new InvalidChecklistItemException("projectScope");
        }
        if (scope == ChecklistScope.ROUND
                && (performanceRoundId == null
                || performanceRoundId < 1)) {
            throw new InvalidChecklistItemException("roundScope");
        }
    }

    private void validateCompleted() {
        if (completed && (completedByMemberId == null
                || completedDttm == null)) {
            throw new InvalidChecklistItemException("completed");
        }
        if (!completed && (completedByMemberId != null
                || completedDttm != null)) {
            throw new InvalidChecklistItemException("notCompleted");
        }
    }

    private static Long requireId(Long value, String field) {
        if (value == null || value < 1) {
            throw new InvalidChecklistItemException(field);
        }
        return value;
    }

    private static ChecklistScope requireScope(ChecklistScope value) {
        if (value == null) {
            throw new InvalidChecklistItemException("scope");
        }
        return value;
    }

    private static String requireContent(String value) {
        if (value == null || value.isBlank()
                || value.strip().length() > MAX_CONTENT_LENGTH) {
            throw new InvalidChecklistItemException("content");
        }
        return value.strip();
    }

    private static int requireOrder(Integer value) {
        if (value == null || value < 0) {
            throw new InvalidChecklistItemException("displayOrder");
        }
        return value;
    }

    private static LocalDateTime requireTime(LocalDateTime value) {
        if (value == null) {
            throw new InvalidChecklistItemException("completedDttm");
        }
        return value;
    }
}
