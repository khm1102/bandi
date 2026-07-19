package kr.ac.tukorea.bandi.domain.performance.dto.response;

import kr.ac.tukorea.bandi.domain.performance.model.AccessibilitySupportType;

public record PerformanceRoundAccessibilityResponse(
        Long performanceRoundAccessibilityId,
        Long performanceRoundId,
        AccessibilitySupportType supportType,
        String title,
        String description,
        int displayOrder
) {
}
