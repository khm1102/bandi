package kr.ac.tukorea.bandi.domain.performance.dto.response;

import kr.ac.tukorea.bandi.domain.performance.model.CastType;
import kr.ac.tukorea.bandi.domain.performance.model.CharacterImportance;

public record PublicPerformanceRoundCastResponse(
        Long performanceRoundCastId,
        Long performanceRoundId,
        Long performanceCharacterId,
        String characterName,
        String characterDescription,
        CharacterImportance characterImportance,
        CastType castType,
        PublicProfileViewResponse profile
) {

    public static PublicPerformanceRoundCastResponse from(
            PerformanceRoundCastResponse cast,
            PublicProfileViewResponse profile) {
        return new PublicPerformanceRoundCastResponse(
                cast.performanceRoundCastId(), cast.performanceRoundId(),
                cast.performanceCharacterId(), cast.characterName(),
                cast.characterDescription(), cast.characterImportance(),
                cast.castType(), profile);
    }
}
