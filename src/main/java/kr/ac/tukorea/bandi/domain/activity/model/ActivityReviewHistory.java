package kr.ac.tukorea.bandi.domain.activity.model;

import kr.ac.tukorea.bandi.domain.activity.exception.InvalidActivityRecordException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ActivityReviewHistory {

    private Long activityReviewHistoryId;
    private final Long activityRecordId;
    private final ActivityRecordStatus previousStatus;
    private final ActivityRecordStatus newStatus;
    private final String comment;
    private final Long reviewedByMemberId;
    private final LocalDateTime reviewedDttm;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;

    public ActivityReviewHistory(Long activityReviewHistoryId,
                                 Long activityRecordId,
                                 ActivityRecordStatus previousStatus,
                                 ActivityRecordStatus newStatus,
                                 String comment, Long reviewedByMemberId,
                                 LocalDateTime reviewedDttm,
                                 LocalDateTime createdDttm,
                                 LocalDateTime updatedDttm) {
        String normalizedComment = normalize(comment);
        if (activityRecordId == null || previousStatus == null || newStatus == null
                || previousStatus == newStatus || reviewedByMemberId == null
                || reviewedDttm == null
                || (newStatus == ActivityRecordStatus.REVISION_REQUESTED
                && normalizedComment == null)) {
            throw new InvalidActivityRecordException("review-history");
        }
        this.activityReviewHistoryId = activityReviewHistoryId;
        this.activityRecordId = activityRecordId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.comment = normalizedComment;
        this.reviewedByMemberId = reviewedByMemberId;
        this.reviewedDttm = reviewedDttm;
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
    }

    public static ActivityReviewHistory change(Long activityRecordId,
                                               ActivityRecordStatus previousStatus,
                                               ActivityRecordStatus newStatus,
                                               String comment,
                                               Long reviewedByMemberId,
                                               LocalDateTime reviewedDttm) {
        return new ActivityReviewHistory(null, activityRecordId, previousStatus,
                newStatus, comment, reviewedByMemberId, reviewedDttm, null, null);
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
