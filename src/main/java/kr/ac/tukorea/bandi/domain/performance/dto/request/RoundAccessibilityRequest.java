package kr.ac.tukorea.bandi.domain.performance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import kr.ac.tukorea.bandi.domain.performance.model.AccessibilitySupportType;

public record RoundAccessibilityRequest(
        @NotNull AccessibilitySupportType supportType,
        @NotBlank @Size(max = 100) String title,
        String description,
        @PositiveOrZero int displayOrder
) {

    public PerformanceRoundAccessibilityWriteParam toParam(
            Long accessibilityId, Long performanceRoundId) {
        return new PerformanceRoundAccessibilityWriteParam(accessibilityId,
                performanceRoundId, supportType, title, description, displayOrder);
    }
}
