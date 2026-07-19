package kr.ac.tukorea.bandi.domain.performance.model;

import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPerformanceContentException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PerformanceCastHistory {

    private Long performanceCastHistoryId;
    private final Long performanceProjectId;
    private final Long performanceRoundId;
    private final Long performanceCharacterId;
    private final Long previousPublicProfileId;
    private final Long newPublicProfileId;
    private final CastType previousCastType;
    private final CastType newCastType;
    private final CastScope scope;
    private final CastAction action;
    private final String reason;
    private final Long changedByMemberId;
    private final LocalDateTime changedDttm;

    public PerformanceCastHistory(
            Long performanceCastHistoryId, Long performanceProjectId,
            Long performanceRoundId, Long performanceCharacterId,
            Long previousPublicProfileId, Long newPublicProfileId,
            CastType previousCastType, CastType newCastType,
            CastScope scope, CastAction action, String reason,
            Long changedByMemberId, LocalDateTime changedDttm) {
        this.performanceCastHistoryId = performanceCastHistoryId;
        this.performanceProjectId = requireId(
                performanceProjectId, "performanceProjectId");
        this.performanceRoundId = performanceRoundId;
        this.performanceCharacterId = requireId(
                performanceCharacterId, "performanceCharacterId");
        this.previousPublicProfileId = previousPublicProfileId;
        this.newPublicProfileId = newPublicProfileId;
        this.previousCastType = previousCastType;
        this.newCastType = newCastType;
        this.scope = requireScope(scope);
        this.action = requireAction(action);
        this.reason = optionalReason(reason);
        this.changedByMemberId = requireId(
                changedByMemberId, "changedByMemberId");
        this.changedDttm = requireTime(changedDttm);
        validateProfiles();
        validateScope();
    }

    public static PerformanceCastHistory project(
            Long performanceProjectId, Long performanceCharacterId,
            Long previousPublicProfileId, Long newPublicProfileId,
            CastType previousCastType, CastType newCastType,
            CastAction action, String reason, Long changedByMemberId,
            LocalDateTime changedDttm) {
        return new PerformanceCastHistory(null, performanceProjectId,
                null, performanceCharacterId, previousPublicProfileId,
                newPublicProfileId, previousCastType, newCastType,
                CastScope.PROJECT, action, reason, changedByMemberId,
                changedDttm);
    }

    public static PerformanceCastHistory round(
            Long performanceProjectId, Long performanceRoundId,
            Long performanceCharacterId,
            Long previousPublicProfileId, Long newPublicProfileId,
            CastType previousCastType, CastType newCastType,
            CastAction action, String reason, Long changedByMemberId,
            LocalDateTime changedDttm) {
        return new PerformanceCastHistory(null, performanceProjectId,
                performanceRoundId, performanceCharacterId,
                previousPublicProfileId, newPublicProfileId,
                previousCastType, newCastType, CastScope.ROUND,
                action, reason, changedByMemberId, changedDttm);
    }

    private void validateProfiles() {
        if (previousPublicProfileId == null && newPublicProfileId == null) {
            throw new InvalidPerformanceContentException("profiles");
        }
        if (action == CastAction.ASSIGN
                && (previousPublicProfileId != null
                || newPublicProfileId == null
                || previousCastType != null || newCastType == null)) {
            throw new InvalidPerformanceContentException("assign");
        }
        if (action == CastAction.REMOVE
                && (previousPublicProfileId == null
                || newPublicProfileId != null
                || previousCastType == null || newCastType != null)) {
            throw new InvalidPerformanceContentException("remove");
        }
        if (action == CastAction.CHANGE
                && (previousPublicProfileId == null
                || newPublicProfileId == null
                || previousCastType == null || newCastType == null)) {
            throw new InvalidPerformanceContentException("change");
        }
    }

    private void validateScope() {
        if (scope == CastScope.PROJECT && performanceRoundId != null) {
            throw new InvalidPerformanceContentException("scope");
        }
        if (scope == CastScope.ROUND && performanceRoundId == null) {
            throw new InvalidPerformanceContentException("scope");
        }
    }

    private static Long requireId(Long value, String field) {
        if (value == null || value < 1) {
            throw new InvalidPerformanceContentException(field);
        }
        return value;
    }

    private static CastScope requireScope(CastScope value) {
        if (value == null) {
            throw new InvalidPerformanceContentException("scope");
        }
        return value;
    }

    private static CastAction requireAction(CastAction value) {
        if (value == null) {
            throw new InvalidPerformanceContentException("action");
        }
        return value;
    }

    private static LocalDateTime requireTime(LocalDateTime value) {
        if (value == null) {
            throw new InvalidPerformanceContentException("changedDttm");
        }
        return value;
    }

    private static String optionalReason(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (value.length() > 500) {
            throw new InvalidPerformanceContentException("reason");
        }
        return value;
    }
}
