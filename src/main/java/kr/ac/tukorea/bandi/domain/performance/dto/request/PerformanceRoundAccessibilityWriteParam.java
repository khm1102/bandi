package kr.ac.tukorea.bandi.domain.performance.dto.request;

import kr.ac.tukorea.bandi.domain.performance.model.AccessibilitySupportType;

public record PerformanceRoundAccessibilityWriteParam(
        Long performanceRoundAccessibilityId,
        Long performanceRoundId,
        AccessibilitySupportType supportType,
        String title,
        String description,
        int displayOrder
) {
}
