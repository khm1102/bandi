package kr.ac.tukorea.bandi.domain.production.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kr.ac.tukorea.bandi.domain.production.model.ProductionTaskStatus;

public record ProductionTaskStatusRequest(
        @NotNull ProductionTaskStatus status,
        @Size(max = 500) String blockedReason,
        @Size(max = 500) String comment
) {
}
