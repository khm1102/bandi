package kr.ac.tukorea.bandi.domain.production.dto.request;

import kr.ac.tukorea.bandi.domain.production.model.ProductionTaskStatus;

public record ProductionTaskStatusParam(
        Long productionTaskId,
        ProductionTaskStatus status,
        String blockedReason,
        String comment
) {
}
