package kr.ac.tukorea.bandi.domain.performance.model;

import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPerformanceContentException;
import lombok.Getter;

@Getter
public class PerformanceCharacter {

    private Long performanceCharacterId;
    private final Long performanceProjectId;
    private final String name;
    private final String description;
    private final CharacterImportance importance;
    private final int displayOrder;

    public PerformanceCharacter(Long performanceCharacterId,
                                Long performanceProjectId, String name,
                                String description,
                                CharacterImportance importance,
                                Integer displayOrder) {
        this.performanceCharacterId = performanceCharacterId;
        this.performanceProjectId = requireId(
                performanceProjectId, "performanceProjectId");
        this.name = requireText(name, 100, "name");
        this.description = optionalText(description, 10_000,
                "description");
        this.importance = requireImportance(importance);
        this.displayOrder = requireOrder(displayOrder);
    }

    public static PerformanceCharacter create(
            Long performanceProjectId, String name, String description,
            CharacterImportance importance, int displayOrder) {
        return new PerformanceCharacter(null, performanceProjectId, name,
                description, importance, displayOrder);
    }

    public PerformanceCharacter edit(String name, String description,
                                     CharacterImportance importance,
                                     int displayOrder) {
        return new PerformanceCharacter(performanceCharacterId,
                performanceProjectId, name, description, importance,
                displayOrder);
    }

    public void validateProject(Long performanceProjectId) {
        if (!this.performanceProjectId.equals(performanceProjectId)) {
            throw new InvalidPerformanceContentException(
                    "performanceProjectId");
        }
    }

    private static Long requireId(Long value, String field) {
        if (value == null || value < 1) {
            throw new InvalidPerformanceContentException(field);
        }
        return value;
    }

    private static String requireText(String value, int maxLength,
                                      String field) {
        if (value == null || value.isBlank()
                || value.strip().length() > maxLength) {
            throw new InvalidPerformanceContentException(field);
        }
        return value.strip();
    }

    private static String optionalText(String value, int maxLength,
                                       String field) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (value.length() > maxLength) {
            throw new InvalidPerformanceContentException(field);
        }
        return value;
    }

    private static CharacterImportance requireImportance(
            CharacterImportance value) {
        if (value == null) {
            throw new InvalidPerformanceContentException("importance");
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
