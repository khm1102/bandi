package kr.ac.tukorea.bandi.domain.performance.model;

import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPerformanceContentException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PerformanceRoundCast {

    private Long performanceRoundCastId;
    private final Long performanceProjectId;
    private final Long performanceRoundId;
    private final Long performanceCharacterId;
    private final Long publicProfileId;
    private final CastType castType;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;

    public PerformanceRoundCast(
            Long performanceRoundCastId, Long performanceProjectId,
            Long performanceRoundId, Long performanceCharacterId,
            Long publicProfileId, CastType castType,
            LocalDateTime createdDttm, LocalDateTime updatedDttm) {
        this.performanceRoundCastId = performanceRoundCastId;
        this.performanceProjectId = requireId(
                performanceProjectId, "performanceProjectId");
        this.performanceRoundId = requireId(
                performanceRoundId, "performanceRoundId");
        this.performanceCharacterId = requireId(
                performanceCharacterId, "performanceCharacterId");
        this.publicProfileId = requireId(
                publicProfileId, "publicProfileId");
        this.castType = requireType(castType);
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
    }

    public static PerformanceRoundCast assign(
            Long performanceProjectId, Long performanceRoundId,
            Long performanceCharacterId, Long publicProfileId,
            CastType castType) {
        return new PerformanceRoundCast(null, performanceProjectId,
                performanceRoundId, performanceCharacterId,
                publicProfileId, castType, null, null);
    }

    public PerformanceRoundCast change(Long publicProfileId,
                                       CastType castType) {
        if (this.publicProfileId.equals(publicProfileId)
                && this.castType == castType) {
            throw new InvalidPerformanceContentException("noChange");
        }
        return new PerformanceRoundCast(performanceRoundCastId,
                performanceProjectId, performanceRoundId,
                performanceCharacterId, publicProfileId, castType,
                createdDttm, updatedDttm);
    }

    private static Long requireId(Long value, String field) {
        if (value == null || value < 1) {
            throw new InvalidPerformanceContentException(field);
        }
        return value;
    }

    private static CastType requireType(CastType value) {
        if (value == null) {
            throw new InvalidPerformanceContentException("castType");
        }
        return value;
    }
}
