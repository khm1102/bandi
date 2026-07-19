package kr.ac.tukorea.bandi.domain.performance.dto.response;

import kr.ac.tukorea.bandi.domain.performance.model.CastAction;
import kr.ac.tukorea.bandi.domain.performance.model.CastScope;
import kr.ac.tukorea.bandi.domain.performance.model.CastType;

import java.time.LocalDateTime;

public record PerformanceCastHistoryResponse(
        Long performanceCastHistoryId,
        Long performanceProjectId,
        Long performanceRoundId,
        Long performanceCharacterId,
        Long previousPublicProfileId,
        Long newPublicProfileId,
        CastType previousCastType,
        CastType newCastType,
        CastScope scope,
        CastAction action,
        String reason,
        Long changedByMemberId,
        LocalDateTime changedDttm
) {
}
