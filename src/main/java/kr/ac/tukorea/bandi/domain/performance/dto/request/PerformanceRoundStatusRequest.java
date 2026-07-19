package kr.ac.tukorea.bandi.domain.performance.dto.request;

import jakarta.validation.constraints.NotNull;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceRoundStatus;

public record PerformanceRoundStatusRequest(
        @NotNull PerformanceRoundStatus status
) {
}
