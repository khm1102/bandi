package kr.ac.tukorea.bandi.domain.performance.dto.response;

import kr.ac.tukorea.bandi.domain.performance.model.CharacterImportance;

public record PerformanceCharacterResponse(
        Long performanceCharacterId,
        Long performanceProjectId,
        String name,
        String description,
        CharacterImportance importance,
        int displayOrder
) {
}
