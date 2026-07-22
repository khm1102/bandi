package kr.ac.tukorea.bandi.domain.notice.model;

import kr.ac.tukorea.bandi.domain.notice.exception.InvalidPublicNoticeException;
import kr.ac.tukorea.bandi.domain.notice.exception.InvalidPublicNoticeStateException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PublicNotice {

    private static final int MAX_TITLE_LENGTH = 200;

    private Long publicNoticeId;
    private final String categoryCode;
    private final String title;
    private final String body;
    private final PublicNoticeStatus status;
    private final boolean pinned;
    private final LocalDateTime publishStartDttm;
    private final LocalDateTime publishEndDttm;
    private final Long createdByMemberId;
    private final Long updatedByMemberId;
    private final Long publishedByMemberId;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;
    private final LocalDateTime deletedDttm;

    public PublicNotice(Long publicNoticeId, String categoryCode, String title, String body,
                        PublicNoticeStatus status, boolean pinned,
                        LocalDateTime publishStartDttm, LocalDateTime publishEndDttm,
                        Long createdByMemberId, Long updatedByMemberId,
                        Long publishedByMemberId, LocalDateTime createdDttm,
                        LocalDateTime updatedDttm, LocalDateTime deletedDttm) {
        validate(categoryCode, title, body, status, publishStartDttm, publishEndDttm,
                createdByMemberId, updatedByMemberId);
        this.publicNoticeId = publicNoticeId;
        this.categoryCode = categoryCode.strip();
        this.title = title.strip();
        this.body = body;
        this.status = status;
        this.pinned = pinned;
        this.publishStartDttm = publishStartDttm;
        this.publishEndDttm = publishEndDttm;
        this.createdByMemberId = createdByMemberId;
        this.updatedByMemberId = updatedByMemberId;
        this.publishedByMemberId = publishedByMemberId;
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
        this.deletedDttm = deletedDttm;
    }

    public static PublicNotice draft(String categoryCode, String title, String body,
                                     boolean pinned, Long actorMemberId) {
        return new PublicNotice(null, categoryCode, title, body, PublicNoticeStatus.DRAFT,
                pinned, null, null, actorMemberId, actorMemberId, null,
                null, null, null);
    }

    public PublicNotice edit(String newCategoryCode, String newTitle, String newBody,
                             boolean newPinned, Long actorMemberId) {
        if (!status.canEdit()) {
            throw new InvalidPublicNoticeStateException(status);
        }
        return copy(newCategoryCode, newTitle, newBody, status, newPinned,
                publishStartDttm, publishEndDttm, actorMemberId, publishedByMemberId);
    }

    public PublicNotice publish(LocalDateTime requestedStartDttm,
                                LocalDateTime requestedEndDttm,
                                Long actorMemberId, LocalDateTime currentDttm) {
        if (!status.canPublish()) {
            throw new InvalidPublicNoticeStateException(status);
        }
        if (actorMemberId == null || currentDttm == null) {
            throw new InvalidPublicNoticeException("publish-actor-time");
        }
        LocalDateTime effectiveStart = requestedStartDttm == null
                ? currentDttm
                : requestedStartDttm;
        validatePeriod(effectiveStart, requestedEndDttm);
        PublicNoticeStatus nextStatus = effectiveStart.isAfter(currentDttm)
                ? PublicNoticeStatus.SCHEDULED
                : PublicNoticeStatus.PUBLISHED;
        return copy(categoryCode, title, body, nextStatus, pinned,
                effectiveStart, requestedEndDttm, actorMemberId, actorMemberId);
    }

    public PublicNotice close(Long actorMemberId) {
        if (!status.canClose()) {
            throw new InvalidPublicNoticeStateException(status);
        }
        return copy(categoryCode, title, body, PublicNoticeStatus.CLOSED, pinned,
                publishStartDttm, publishEndDttm, actorMemberId, publishedByMemberId);
    }

    public PublicNotice archive(Long actorMemberId) {
        if (!status.canArchive()) {
            throw new InvalidPublicNoticeStateException(status);
        }
        return copy(categoryCode, title, body, PublicNoticeStatus.ARCHIVED, pinned,
                publishStartDttm, publishEndDttm, actorMemberId, publishedByMemberId);
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

    private PublicNotice copy(String newCategoryCode, String newTitle, String newBody,
                              PublicNoticeStatus newStatus, boolean newPinned,
                              LocalDateTime newPublishStartDttm,
                              LocalDateTime newPublishEndDttm, Long actorMemberId,
                              Long newPublishedByMemberId) {
        return new PublicNotice(publicNoticeId, newCategoryCode, newTitle, newBody,
                newStatus, newPinned, newPublishStartDttm, newPublishEndDttm,
                createdByMemberId, actorMemberId, newPublishedByMemberId,
                createdDttm, updatedDttm, deletedDttm);
    }

    private void validate(String categoryCodeValue, String titleValue, String bodyValue,
                          PublicNoticeStatus statusValue,
                          LocalDateTime publishStartValue, LocalDateTime publishEndValue,
                          Long creatorId, Long updaterId) {
        validateCategory(categoryCodeValue);
        validateText(titleValue, MAX_TITLE_LENGTH, "title");
        validateText(bodyValue, Integer.MAX_VALUE, "body");
        if (statusValue == null || creatorId == null || updaterId == null) {
            throw new InvalidPublicNoticeException("state-actor");
        }
        validatePeriod(publishStartValue, publishEndValue);
    }

    private void validatePeriod(LocalDateTime startDttm, LocalDateTime endDttm) {
        if (startDttm != null && endDttm != null && endDttm.isBefore(startDttm)) {
            throw new InvalidPublicNoticeException("publish-period");
        }
    }

    private void validateText(String value, int maxLength, String field) {
        if (value == null || value.isBlank() || value.length() > maxLength) {
            throw new InvalidPublicNoticeException(field);
        }
    }

    private void validateCategory(String categoryCodeValue) {
        if (!PublicNoticeCategory.isSupported(categoryCodeValue)) {
            throw new InvalidPublicNoticeException("categoryCode");
        }
    }
}
