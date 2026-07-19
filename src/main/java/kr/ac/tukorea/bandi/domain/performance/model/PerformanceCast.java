package kr.ac.tukorea.bandi.domain.performance.model;

import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPerformanceContentException;
import lombok.Getter;

@Getter
public class PerformanceCast {

    private Long performanceCastId;
    private final Long performanceProjectId;
    private final Long performanceCharacterId;
    private final Long publicProfileId;
    private final CastType castType;
    private final int displayOrder;

    public PerformanceCast(Long performanceCastId,
                           Long performanceProjectId,
                           Long performanceCharacterId,
                           Long publicProfileId, CastType castType,
                           Integer displayOrder) {
        this.performanceCastId = performanceCastId;
        this.performanceProjectId = requireId(
                performanceProjectId, "performanceProjectId");
        this.performanceCharacterId = requireId(
                performanceCharacterId, "performanceCharacterId");
        this.publicProfileId = requireId(
                publicProfileId, "publicProfileId");
        this.castType = requireType(castType);
        this.displayOrder = requireOrder(displayOrder);
    }

    public static PerformanceCast assign(
            Long performanceProjectId, Long performanceCharacterId,
            Long publicProfileId, CastType castType, int displayOrder) {
        return new PerformanceCast(null, performanceProjectId,
                performanceCharacterId, publicProfileId, castType,
                displayOrder);
    }

    public PerformanceCast change(Long publicProfileId,
                                  CastType castType, int displayOrder) {
        if (this.publicProfileId.equals(publicProfileId)
                && this.castType == castType
                && this.displayOrder == displayOrder) {
            throw new InvalidPerformanceContentException("noChange");
        }
        return new PerformanceCast(performanceCastId,
                performanceProjectId, performanceCharacterId,
                publicProfileId, castType, displayOrder);
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

    private static int requireOrder(Integer value) {
        if (value == null || value < 0) {
            throw new InvalidPerformanceContentException("displayOrder");
        }
        return value;
    }
}
