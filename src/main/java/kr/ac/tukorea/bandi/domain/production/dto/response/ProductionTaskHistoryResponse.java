package kr.ac.tukorea.bandi.domain.production.dto.response;

import kr.ac.tukorea.bandi.domain.production.model.ProductionTaskStatus;

import java.time.LocalDateTime;

public record ProductionTaskHistoryResponse(
        Long productionTaskHistoryId,
        ProductionTaskStatus previousStatus,
        ProductionTaskStatus newStatus,
        String comment,
        Long changedByMemberId,
        String changedByName,
        LocalDateTime changedDttm
) {
}
