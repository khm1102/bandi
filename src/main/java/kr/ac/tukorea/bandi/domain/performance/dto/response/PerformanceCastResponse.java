package kr.ac.tukorea.bandi.domain.performance.dto.response;

import kr.ac.tukorea.bandi.domain.performance.model.CastType;
import kr.ac.tukorea.bandi.domain.performance.model.CharacterImportance;

public record PerformanceCastResponse(
        Long performanceCastId,
        Long performanceProjectId,
        Long performanceCharacterId,
        String characterName,
        String characterDescription,
        CharacterImportance characterImportance,
        Long publicProfileId,
        CastType castType,
        int displayOrder
) {
}
