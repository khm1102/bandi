package kr.ac.tukorea.bandi.domain.performance.dto.request;

import kr.ac.tukorea.bandi.domain.performance.model.CharacterImportance;

public record PerformanceCharacterWriteParam(
        Long performanceCharacterId,
        Long performanceProjectId,
        String name,
        String description,
        CharacterImportance importance,
        int displayOrder
) {
}
