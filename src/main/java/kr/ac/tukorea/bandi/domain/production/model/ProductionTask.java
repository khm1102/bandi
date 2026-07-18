package kr.ac.tukorea.bandi.domain.production.model;

import kr.ac.tukorea.bandi.domain.production.exception.InvalidProductionTaskException;
import kr.ac.tukorea.bandi.domain.production.exception.InvalidProductionTaskStateException;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
public class ProductionTask {

    private static final int MAX_TITLE_LENGTH = 200;
    private static final int MAX_BLOCKED_REASON_LENGTH = 500;

    private Long productionTaskId;
    private final Long performanceProjectId;
    private final Long teamId;
    private final String title;
    private final String description;
    private final LocalDate startDate;
    private final LocalDate dueDate;
    private final ProductionTaskStatus status;
    private final String blockedReason;
    private final Long createdByMemberId;
    private final Long updatedByMemberId;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;
    private final LocalDateTime deletedDttm;

    public ProductionTask(Long productionTaskId, Long performanceProjectId,
                          Long teamId, String title, String description,
                          LocalDate startDate, LocalDate dueDate,
                          ProductionTaskStatus status, String blockedReason,
                          Long createdByMemberId, Long updatedByMemberId,
                          LocalDateTime createdDttm,
                          LocalDateTime updatedDttm,
                          LocalDateTime deletedDttm) {
        String normalizedBlockedReason = normalize(blockedReason);
        validate(performanceProjectId, teamId, title, startDate, dueDate,
                status, normalizedBlockedReason,
                createdByMemberId, updatedByMemberId);
        this.productionTaskId = productionTaskId;
        this.performanceProjectId = performanceProjectId;
        this.teamId = teamId;
        this.title = title.strip();
        this.description = normalize(description);
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.status = status;
        this.blockedReason = normalizedBlockedReason;
        this.createdByMemberId = createdByMemberId;
        this.updatedByMemberId = updatedByMemberId;
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
        this.deletedDttm = deletedDttm;
    }

    public static ProductionTask todo(Long performanceProjectId, Long teamId,
                                      String title, String description,
                                      LocalDate startDate, LocalDate dueDate,
                                      Long actorMemberId) {
        return new ProductionTask(null, performanceProjectId, teamId,
                title, description, startDate, dueDate,
                ProductionTaskStatus.TODO, null, actorMemberId,
                actorMemberId, null, null, null);
    }

    public ProductionTask edit(String newTitle, String newDescription,
                               LocalDate newStartDate, LocalDate newDueDate,
                               Long actorMemberId) {
        return copy(newTitle, newDescription, newStartDate, newDueDate,
                status, blockedReason, actorMemberId);
    }

    public ProductionTask changeStatus(ProductionTaskStatus newStatus,
                                       String newBlockedReason,
                                       Long actorMemberId) {
        if (newStatus == null || newStatus == status) {
            throw new InvalidProductionTaskStateException("status-change");
        }
        String nextBlockedReason = newStatus == ProductionTaskStatus.BLOCKED
                ? newBlockedReason : null;
        return copy(title, description, startDate, dueDate,
                newStatus, nextBlockedReason, actorMemberId);
    }

    private ProductionTask copy(String newTitle, String newDescription,
                                LocalDate newStartDate, LocalDate newDueDate,
                                ProductionTaskStatus newStatus,
                                String newBlockedReason,
                                Long actorMemberId) {
        return new ProductionTask(productionTaskId, performanceProjectId,
                teamId, newTitle, newDescription, newStartDate, newDueDate,
                newStatus, newBlockedReason, createdByMemberId,
                actorMemberId, createdDttm, updatedDttm, deletedDttm);
    }

    private void validate(Long projectId, Long targetTeamId,
                          String targetTitle, LocalDate targetStartDate,
                          LocalDate targetDueDate,
                          ProductionTaskStatus targetStatus,
                          String targetBlockedReason,
                          Long creatorId, Long updaterId) {
        if (projectId == null || targetTeamId == null
                || targetTitle == null || targetTitle.isBlank()
                || targetTitle.strip().length() > MAX_TITLE_LENGTH
                || (targetStartDate != null && targetDueDate != null
                && targetDueDate.isBefore(targetStartDate))
                || targetStatus == null || creatorId == null
                || updaterId == null) {
            throw new InvalidProductionTaskException("required");
        }
        if (targetStatus == ProductionTaskStatus.BLOCKED
                && (targetBlockedReason == null
                || targetBlockedReason.length() > MAX_BLOCKED_REASON_LENGTH)) {
            throw new InvalidProductionTaskException("blocked-reason");
        }
        if (targetStatus != ProductionTaskStatus.BLOCKED
                && targetBlockedReason != null) {
            throw new InvalidProductionTaskException("unexpected-blocked-reason");
        }
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
