package kr.ac.tukorea.bandi.domain.activity.dto.response;

import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecordStatus;

import java.time.LocalDateTime;

public record ActivityRecordSummaryResponse(
        Long activityRecordId,
        Long teamId,
        String teamName,
        LocalDateTime activityDttm,
        String title,
        int participantCount,
        ActivityRecordStatus status,
        String createdByName,
        Long representativeStoredFileId,
        LocalDateTime updatedDttm
) {
}
