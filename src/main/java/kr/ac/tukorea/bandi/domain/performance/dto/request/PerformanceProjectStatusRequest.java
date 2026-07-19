package kr.ac.tukorea.bandi.domain.performance.dto.request;

import jakarta.validation.constraints.NotNull;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceProjectStatus;

public record PerformanceProjectStatusRequest(
        @NotNull PerformanceProjectStatus status
) {
}
