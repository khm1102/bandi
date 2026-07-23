package kr.ac.tukorea.bandi.domain.activity.dto.response;

import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecordStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ActivityRecordManageDetailResponse(
        Long activityRecordId,
        Long teamId,
        String teamName,
        LocalDateTime activityDttm,
        String title,
        String body,
        int participantCount,
        ActivityRecordStatus status,
        Long createdByMemberId,
        String createdByName,
        String updatedByName,
        LocalDateTime submittedDttm,
        LocalDateTime reviewedDttm,
        LocalDateTime updatedDttm,
        boolean reportDocument,
        List<ActivityFileResponse> currentFiles,
        List<ActivityRevisionResponse> revisions,
        List<ActivityReviewHistoryResponse> reviewHistories
) {

    public static ActivityRecordManageDetailResponse of(
            ActivityRecordManageContentResponse content,
            List<ActivityFileResponse> currentFiles,
            List<ActivityRevisionResponse> revisions,
            List<ActivityReviewHistoryResponse> histories) {
        return new ActivityRecordManageDetailResponse(content.activityRecordId(),
                content.teamId(), content.teamName(), content.activityDttm(),
                content.title(), content.body(), content.participantCount(),
                content.status(), content.createdByMemberId(), content.createdByName(),
                content.updatedByName(), content.submittedDttm(), content.reviewedDttm(),
                content.updatedDttm(), content.reportDocument(), List.copyOf(currentFiles),
                List.copyOf(revisions), List.copyOf(histories));
    }
}
