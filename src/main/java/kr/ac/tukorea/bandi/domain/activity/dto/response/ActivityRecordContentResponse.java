package kr.ac.tukorea.bandi.domain.activity.dto.response;

import java.time.LocalDateTime;

public record ActivityRecordContentResponse(
        Long activityRecordId,
        Long teamId,
        String teamName,
        LocalDateTime activityDttm,
        String title,
        String body,
        int participantCount,
        String createdByName,
        LocalDateTime updatedDttm
) {
}
