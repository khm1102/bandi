package kr.ac.tukorea.bandi.domain.activity.dto.request;

import java.time.LocalDateTime;

public record ActivityRecordWriteParam(
        Long teamId,
        LocalDateTime activityDttm,
        String title,
        String body,
        int participantCount
) {
}
