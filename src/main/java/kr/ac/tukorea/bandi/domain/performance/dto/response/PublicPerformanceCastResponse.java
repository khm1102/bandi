package kr.ac.tukorea.bandi.domain.performance.dto.response;

import kr.ac.tukorea.bandi.domain.performance.model.CastType;
import kr.ac.tukorea.bandi.domain.performance.model.CharacterImportance;

public record PublicPerformanceCastResponse(
        Long performanceCastId,
        Long performanceCharacterId,
        String characterName,
        String characterDescription,
        CharacterImportance characterImportance,
        CastType castType,
        int displayOrder,
        PublicProfileViewResponse profile
) {

    public static PublicPerformanceCastResponse from(
            PerformanceCastResponse cast,
            PublicProfileViewResponse profile) {
        return new PublicPerformanceCastResponse(cast.performanceCastId(),
                cast.performanceCharacterId(), cast.characterName(),
                cast.characterDescription(), cast.characterImportance(),
                cast.castType(), cast.displayOrder(), profile);
    }
}
