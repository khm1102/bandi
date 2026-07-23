package kr.ac.tukorea.bandi.domain.activity.dto.response;

import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecordStatus;

import java.time.LocalDateTime;

public record ActivityRecordManageContentResponse(
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
        boolean reportDocument
) {
}
