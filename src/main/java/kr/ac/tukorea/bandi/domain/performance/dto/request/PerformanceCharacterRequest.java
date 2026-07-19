package kr.ac.tukorea.bandi.domain.performance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import kr.ac.tukorea.bandi.domain.performance.model.CharacterImportance;

public record PerformanceCharacterRequest(
        @NotNull @Positive Long performanceProjectId,
        @NotBlank @Size(max = 100) String name,
        String description,
        @NotNull CharacterImportance importance,
        @PositiveOrZero int displayOrder
) {

    public PerformanceCharacterWriteParam toParam(Long characterId) {
        return new PerformanceCharacterWriteParam(characterId,
                performanceProjectId, name, description, importance,
                displayOrder);
    }
}
