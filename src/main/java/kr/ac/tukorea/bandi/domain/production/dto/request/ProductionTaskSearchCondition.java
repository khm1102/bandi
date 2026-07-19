package kr.ac.tukorea.bandi.domain.production.dto.request;

import kr.ac.tukorea.bandi.domain.production.exception.InvalidProductionTaskException;
import kr.ac.tukorea.bandi.domain.production.model.ProductionTaskStatus;

public record ProductionTaskSearchCondition(
        Long performanceProjectId,
        Long teamId,
        ProductionTaskStatus status,
        boolean overdueOnly,
        int offset,
        int limit
) {

    public ProductionTaskSearchCondition {
        if (performanceProjectId == null || offset < 0
                || limit < 1 || limit > 100) {
            throw new InvalidProductionTaskException("search-condition");
        }
    }
}
