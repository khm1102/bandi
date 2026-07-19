package kr.ac.tukorea.bandi.domain.performance.dto.response;

import kr.ac.tukorea.bandi.domain.performance.model.CastType;
import kr.ac.tukorea.bandi.domain.performance.model.CharacterImportance;

public record PerformanceRoundCastResponse(
        Long performanceRoundCastId,
        Long performanceProjectId,
        Long performanceRoundId,
        Long performanceCharacterId,
        String characterName,
        String characterDescription,
        CharacterImportance characterImportance,
        Long publicProfileId,
        CastType castType
) {
}
