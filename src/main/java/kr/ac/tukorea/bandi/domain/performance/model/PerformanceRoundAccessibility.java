package kr.ac.tukorea.bandi.domain.performance.model;

import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPerformanceContentException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PerformanceRoundAccessibility {

    private Long performanceRoundAccessibilityId;
    private final Long performanceRoundId;
    private final AccessibilitySupportType supportType;
    private final String title;
    private final String description;
    private final int displayOrder;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;

    public PerformanceRoundAccessibility(
            Long performanceRoundAccessibilityId,
            Long performanceRoundId,
            AccessibilitySupportType supportType, String title,
            String description, Integer displayOrder,
            LocalDateTime createdDttm, LocalDateTime updatedDttm) {
        this.performanceRoundAccessibilityId =
                performanceRoundAccessibilityId;
        this.performanceRoundId = requireId(performanceRoundId);
        this.supportType = requireType(supportType);
        this.title = requireTitle(title);
        this.description = optionalDescription(description);
        this.displayOrder = requireOrder(displayOrder);
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
    }

    public static PerformanceRoundAccessibility create(
            Long performanceRoundId,
            AccessibilitySupportType supportType, String title,
            String description, int displayOrder) {
        return new PerformanceRoundAccessibility(null,
                performanceRoundId, supportType, title, description,
                displayOrder, null, null);
    }

    public PerformanceRoundAccessibility edit(
            AccessibilitySupportType supportType, String title,
            String description, int displayOrder) {
        return new PerformanceRoundAccessibility(
                performanceRoundAccessibilityId, performanceRoundId,
                supportType, title, description, displayOrder,
                createdDttm, updatedDttm);
    }

    public void validateRound(Long performanceRoundId) {
        if (!this.performanceRoundId.equals(performanceRoundId)) {
            throw new InvalidPerformanceContentException(
                    "performanceRoundId");
        }
    }

    private static Long requireId(Long value) {
        if (value == null || value < 1) {
            throw new InvalidPerformanceContentException(
                    "performanceRoundId");
        }
        return value;
    }

    private static AccessibilitySupportType requireType(
            AccessibilitySupportType value) {
        if (value == null) {
            throw new InvalidPerformanceContentException("supportType");
        }
        return value;
    }

    private static String requireTitle(String value) {
        if (value == null || value.isBlank()
                || value.strip().length() > 100) {
            throw new InvalidPerformanceContentException("title");
        }
        return value.strip();
    }

    private static String optionalDescription(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (value.length() > 10_000) {
            throw new InvalidPerformanceContentException("description");
        }
        return value;
    }

    private static int requireOrder(Integer value) {
        if (value == null || value < 0) {
            throw new InvalidPerformanceContentException("displayOrder");
        }
        return value;
    }
}
