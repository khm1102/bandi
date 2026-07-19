package kr.ac.tukorea.bandi.domain.performance.dto.request;

import kr.ac.tukorea.bandi.domain.performance.model.PerformanceProjectStatus;

public record PerformanceProjectStatusParam(
        Long performanceProjectId,
        PerformanceProjectStatus status
) {
}
