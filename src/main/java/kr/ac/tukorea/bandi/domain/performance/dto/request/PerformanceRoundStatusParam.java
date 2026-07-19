package kr.ac.tukorea.bandi.domain.performance.dto.request;

import kr.ac.tukorea.bandi.domain.performance.model.PerformanceRoundStatus;

public record PerformanceRoundStatusParam(
        Long performanceRoundId,
        PerformanceRoundStatus status
) {
}
