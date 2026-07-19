package kr.ac.tukorea.bandi.domain.activity.dto.request;

import java.time.LocalDateTime;

public record ActivityRecordUpdateParam(
        Long activityRecordId,
        LocalDateTime activityDttm,
        String title,
        String body,
        int participantCount
) {
}
