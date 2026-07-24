package kr.ac.tukorea.bandi.domain.notice.model;

import kr.ac.tukorea.bandi.domain.notice.exception.InvalidInternalNoticeException;
import kr.ac.tukorea.bandi.domain.notice.exception.InvalidInternalNoticeStateException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InternalNotice {

    private static final int MAX_TITLE_LENGTH = 200;

    private Long internalNoticeId;
    private final InternalNoticeTargetScope targetScope;
    private final Long teamId;
    private final String title;
    private final String body;
    private final InternalNoticeStatus status;
    private final boolean important;
    private final LocalDateTime publishStartDttm;
    private final LocalDateTime publishEndDttm;
    private final Long createdByMemberId;
    private final Long updatedByMemberId;
    private final Long publishedByMemberId;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;
    private final LocalDateTime deletedDttm;

    public InternalNotice(Long internalNoticeId, InternalNoticeTargetScope targetScope,
                          Long teamId, String title, String body,
                          InternalNoticeStatus status, boolean important,
                          LocalDateTime publishStartDttm, LocalDateTime publishEndDttm,
                          Long createdByMemberId, Long updatedByMemberId,
                          Long publishedByMemberId, LocalDateTime createdDttm,
                          LocalDateTime updatedDttm, LocalDateTime deletedDttm) {
        validate(targetScope, teamId, title, body, status, publishStartDttm,
                publishEndDttm, createdByMemberId, updatedByMemberId);
        this.internalNoticeId = internalNoticeId;
        this.targetScope = targetScope;
        this.teamId = teamId;
        this.title = title.strip();
        this.body = body;
        this.status = status;
        this.important = important;
        this.publishStartDttm = publishStartDttm;
        this.publishEndDttm = publishEndDttm;
        this.createdByMemberId = createdByMemberId;
        this.updatedByMemberId = updatedByMemberId;
        this.publishedByMemberId = publishedByMemberId;
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
        this.deletedDttm = deletedDttm;
    }

    public static InternalNotice draft(InternalNoticeTargetScope targetScope, Long teamId,
                                       String title, String body, boolean important,
                                       Long actorMemberId) {
        return new InternalNotice(null, targetScope, teamId, title, body,
                InternalNoticeStatus.DRAFT, important, null, null, actorMemberId,
                actorMemberId, null, null, null, null);
    }

    public InternalNotice edit(InternalNoticeTargetScope newTargetScope, Long newTeamId,
                               String newTitle, String newBody, boolean newImportant,
                               Long actorMemberId) {
        if (!status.canEdit()) {
            throw new InvalidInternalNoticeStateException(status);
        }
        return copy(newTargetScope, newTeamId, newTitle, newBody, status, newImportant,
                publishStartDttm, publishEndDttm, actorMemberId, publishedByMemberId);
    }

    public InternalNotice publish(LocalDateTime requestedStartDttm,
                                  LocalDateTime requestedEndDttm,
                                  Long actorMemberId, LocalDateTime currentDttm) {
        if (!status.canPublish()) {
            throw new InvalidInternalNoticeStateException(status);
        }
        if (actorMemberId == null || currentDttm == null) {
            throw new InvalidInternalNoticeException("publish-actor-time");
        }
        LocalDateTime effectiveStart = requestedStartDttm == null
                ? currentDttm
                : requestedStartDttm;
        validatePeriod(effectiveStart, requestedEndDttm);
        InternalNoticeStatus nextStatus = effectiveStart.isAfter(currentDttm)
                ? InternalNoticeStatus.SCHEDULED
                : InternalNoticeStatus.PUBLISHED;
        return copy(targetScope, teamId, title, body, nextStatus, important,
                effectiveStart, requestedEndDttm, actorMemberId, actorMemberId);
    }

    public InternalNotice close(Long actorMemberId) {
        if (!status.canClose()) {
            throw new InvalidInternalNoticeStateException(status);
        }
        return copy(targetScope, teamId, title, body, InternalNoticeStatus.CLOSED,
                important, publishStartDttm, publishEndDttm, actorMemberId,
                publishedByMemberId);
    }

    public InternalNotice archive(Long actorMemberId) {
        if (!status.canArchive()) {
            throw new InvalidInternalNoticeStateException(status);
        }
        return copy(targetScope, teamId, title, body, InternalNoticeStatus.ARCHIVED,
                important, publishStartDttm, publishEndDttm, actorMemberId,
                publishedByMemberId);
    }

    public InternalNotice returnToDraft(Long actorMemberId) {
        if (!status.canReturnToDraft()) {
            throw new InvalidInternalNoticeStateException(status);
        }
        return copy(targetScope, teamId, title, body, InternalNoticeStatus.DRAFT,
                important, null, null, actorMemberId, null);
    }

    public void validateDeletable() {
        if (!status.canDelete()) {
            throw new InvalidInternalNoticeStateException(status);
        }
    }

    public boolean isPubliclyVisible(LocalDateTime currentDttm) {
        if (!status.canBePublic() || currentDttm == null || publishStartDttm == null) {
            return false;
        }
        if (currentDttm.isBefore(publishStartDttm)) {
            return false;
        }
        return publishEndDttm == null || currentDttm.isBefore(publishEndDttm);
    }

    private InternalNotice copy(InternalNoticeTargetScope newTargetScope, Long newTeamId,
                                String newTitle, String newBody,
                                InternalNoticeStatus newStatus, boolean newImportant,
                                LocalDateTime newPublishStartDttm,
                                LocalDateTime newPublishEndDttm, Long actorMemberId,
                                Long newPublishedByMemberId) {
        return new InternalNotice(internalNoticeId, newTargetScope, newTeamId,
                newTitle, newBody, newStatus, newImportant, newPublishStartDttm,
                newPublishEndDttm, createdByMemberId, actorMemberId,
                newPublishedByMemberId, createdDttm, updatedDttm, deletedDttm);
    }

    private void validate(InternalNoticeTargetScope scopeValue, Long teamIdValue,
                          String titleValue, String bodyValue,
                          InternalNoticeStatus statusValue,
                          LocalDateTime publishStartValue,
                          LocalDateTime publishEndValue,
                          Long creatorId, Long updaterId) {
        if (scopeValue == null || !scopeValue.matchesTeam(teamIdValue)) {
            throw new InvalidInternalNoticeException("target");
        }
        validateText(titleValue, MAX_TITLE_LENGTH, "title");
        validateText(bodyValue, Integer.MAX_VALUE, "body");
        if (statusValue == null || creatorId == null || updaterId == null) {
            throw new InvalidInternalNoticeException("state-actor");
        }
        validatePeriod(publishStartValue, publishEndValue);
    }

    private void validatePeriod(LocalDateTime startDttm, LocalDateTime endDttm) {
        if (startDttm != null && endDttm != null && endDttm.isBefore(startDttm)) {
            throw new InvalidInternalNoticeException("publish-period");
        }
    }

    private void validateText(String value, int maxLength, String field) {
        if (value == null || value.isBlank() || value.length() > maxLength) {
            throw new InvalidInternalNoticeException(field);
        }
    }
}
