package kr.ac.tukorea.bandi.domain.production.model;

import kr.ac.tukorea.bandi.domain.production.exception.InvalidProductionTaskException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ProductionTaskHistory {

    private static final int MAX_COMMENT_LENGTH = 500;

    private Long productionTaskHistoryId;
    private final Long productionTaskId;
    private final ProductionTaskStatus previousStatus;
    private final ProductionTaskStatus newStatus;
    private final String comment;
    private final Long changedByMemberId;
    private final LocalDateTime changedDttm;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;

    public ProductionTaskHistory(Long productionTaskHistoryId,
                                 Long productionTaskId,
                                 ProductionTaskStatus previousStatus,
                                 ProductionTaskStatus newStatus,
                                 String comment, Long changedByMemberId,
                                 LocalDateTime changedDttm,
                                 LocalDateTime createdDttm,
                                 LocalDateTime updatedDttm) {
        String normalizedComment = normalize(comment);
        if (productionTaskId == null || previousStatus == null
                || newStatus == null || previousStatus == newStatus
                || changedByMemberId == null || changedDttm == null
                || (normalizedComment != null
                && normalizedComment.length() > MAX_COMMENT_LENGTH)) {
            throw new InvalidProductionTaskException("task-history");
        }
        this.productionTaskHistoryId = productionTaskHistoryId;
        this.productionTaskId = productionTaskId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.comment = normalizedComment;
        this.changedByMemberId = changedByMemberId;
        this.changedDttm = changedDttm;
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
    }

    public static ProductionTaskHistory change(
            Long productionTaskId, ProductionTaskStatus previousStatus,
            ProductionTaskStatus newStatus, String comment,
            Long actorMemberId, LocalDateTime currentDttm) {
        return new ProductionTaskHistory(null, productionTaskId,
                previousStatus, newStatus, comment, actorMemberId,
                currentDttm, null, null);
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
