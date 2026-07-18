package kr.ac.tukorea.bandi.domain.activity.model;

import kr.ac.tukorea.bandi.domain.activity.exception.InvalidActivityRecordException;
import kr.ac.tukorea.bandi.domain.activity.exception.InvalidActivityRecordStateException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ActivityRecord {

    private static final int MAX_TITLE_LENGTH = 150;

    private Long activityRecordId;
    private final Long teamId;
    private final LocalDateTime activityDttm;
    private final String title;
    private final String body;
    private final int participantCount;
    private final ActivityRecordStatus status;
    private final Long createdByMemberId;
    private final Long updatedByMemberId;
    private final LocalDateTime submittedDttm;
    private final LocalDateTime reviewedDttm;
    private final Long reviewedByMemberId;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;
    private final LocalDateTime deletedDttm;

    public ActivityRecord(Long activityRecordId, Long teamId,
                          LocalDateTime activityDttm, String title, String body,
                          int participantCount, ActivityRecordStatus status,
                          Long createdByMemberId, Long updatedByMemberId,
                          LocalDateTime submittedDttm, LocalDateTime reviewedDttm,
                          Long reviewedByMemberId, LocalDateTime createdDttm,
                          LocalDateTime updatedDttm, LocalDateTime deletedDttm) {
        validate(teamId, activityDttm, title, body, participantCount, status,
                createdByMemberId, updatedByMemberId, submittedDttm,
                reviewedDttm, reviewedByMemberId);
        this.activityRecordId = activityRecordId;
        this.teamId = teamId;
        this.activityDttm = activityDttm;
        this.title = title.strip();
        this.body = body.strip();
        this.participantCount = participantCount;
        this.status = status;
        this.createdByMemberId = createdByMemberId;
        this.updatedByMemberId = updatedByMemberId;
        this.submittedDttm = submittedDttm;
        this.reviewedDttm = reviewedDttm;
        this.reviewedByMemberId = reviewedByMemberId;
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
        this.deletedDttm = deletedDttm;
    }

    public static ActivityRecord draft(Long teamId, LocalDateTime activityDttm,
                                       String title, String body, int participantCount,
                                       Long actorMemberId) {
        return new ActivityRecord(null, teamId, activityDttm, title, body,
                participantCount, ActivityRecordStatus.DRAFT, actorMemberId,
                actorMemberId, null, null, null, null, null, null);
    }

    public ActivityRecord edit(LocalDateTime newActivityDttm, String newTitle,
                               String newBody, int newParticipantCount,
                               Long actorMemberId) {
        if (!status.canEdit()) {
            throw new InvalidActivityRecordStateException(status);
        }
        return copy(newActivityDttm, newTitle, newBody, newParticipantCount,
                status, actorMemberId, submittedDttm, reviewedDttm,
                reviewedByMemberId);
    }

    public ActivityRecord submit(Long actorMemberId, LocalDateTime currentDttm) {
        if (!status.canSubmit()) {
            throw new InvalidActivityRecordStateException(status);
        }
        validateActorTime(actorMemberId, currentDttm);
        return copy(activityDttm, title, body, participantCount,
                ActivityRecordStatus.SUBMITTED, actorMemberId, currentDttm,
                null, null);
    }

    public ActivityRecord approve(Long reviewerMemberId, LocalDateTime currentDttm) {
        if (!status.canReview()) {
            throw new InvalidActivityRecordStateException(status);
        }
        validateActorTime(reviewerMemberId, currentDttm);
        return copy(activityDttm, title, body, participantCount,
                ActivityRecordStatus.APPROVED, reviewerMemberId, submittedDttm,
                currentDttm, reviewerMemberId);
    }

    public ActivityRecord requestRevision(Long reviewerMemberId,
                                          LocalDateTime currentDttm) {
        if (!status.canReview()) {
            throw new InvalidActivityRecordStateException(status);
        }
        validateActorTime(reviewerMemberId, currentDttm);
        return copy(activityDttm, title, body, participantCount,
                ActivityRecordStatus.REVISION_REQUESTED, reviewerMemberId,
                submittedDttm, currentDttm, reviewerMemberId);
    }

    public ActivityRecord archive(Long actorMemberId) {
        if (!status.canArchive() || actorMemberId == null) {
            throw new InvalidActivityRecordStateException(status);
        }
        return copy(activityDttm, title, body, participantCount,
                ActivityRecordStatus.ARCHIVED, actorMemberId, submittedDttm,
                reviewedDttm, reviewedByMemberId);
    }

    private ActivityRecord copy(LocalDateTime newActivityDttm, String newTitle,
                                String newBody, int newParticipantCount,
                                ActivityRecordStatus newStatus, Long actorMemberId,
                                LocalDateTime newSubmittedDttm,
                                LocalDateTime newReviewedDttm,
                                Long newReviewedByMemberId) {
        return new ActivityRecord(activityRecordId, teamId, newActivityDttm,
                newTitle, newBody, newParticipantCount, newStatus,
                createdByMemberId, actorMemberId, newSubmittedDttm,
                newReviewedDttm, newReviewedByMemberId, createdDttm,
                updatedDttm, deletedDttm);
    }

    private void validate(Long targetTeamId, LocalDateTime targetActivityDttm,
                          String targetTitle, String targetBody, int targetCount,
                          ActivityRecordStatus targetStatus, Long creatorId,
                          Long updaterId, LocalDateTime targetSubmittedDttm,
                          LocalDateTime targetReviewedDttm, Long reviewerId) {
        if (targetTeamId == null || targetActivityDttm == null
                || targetStatus == null || creatorId == null || updaterId == null
                || targetCount < 1) {
            throw new InvalidActivityRecordException("required");
        }
        validateText(targetTitle, MAX_TITLE_LENGTH, "title");
        validateText(targetBody, Integer.MAX_VALUE, "body");
        validateState(targetStatus, targetSubmittedDttm,
                targetReviewedDttm, reviewerId);
    }

    private void validateState(ActivityRecordStatus targetStatus,
                               LocalDateTime targetSubmittedDttm,
                               LocalDateTime targetReviewedDttm,
                               Long reviewerId) {
        boolean valid = switch (targetStatus) {
            case DRAFT -> targetSubmittedDttm == null
                    && targetReviewedDttm == null && reviewerId == null;
            case SUBMITTED -> targetSubmittedDttm != null
                    && targetReviewedDttm == null && reviewerId == null;
            case APPROVED, REVISION_REQUESTED -> targetSubmittedDttm != null
                    && targetReviewedDttm != null && reviewerId != null;
            case ARCHIVED -> true;
        };
        if (!valid) {
            throw new InvalidActivityRecordException("status-timestamps");
        }
    }

    private void validateActorTime(Long actorMemberId, LocalDateTime currentDttm) {
        if (actorMemberId == null || currentDttm == null) {
            throw new InvalidActivityRecordException("actor-time");
        }
    }

    private void validateText(String value, int maxLength, String field) {
        if (value == null || value.isBlank() || value.length() > maxLength) {
            throw new InvalidActivityRecordException(field);
        }
    }
}
