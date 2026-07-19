package kr.ac.tukorea.bandi.domain.activity.dto.response;

import java.time.LocalDateTime;

public record ActivityRevisionResponse(
        int revisionNo,
        LocalDateTime activityDttm,
        String title,
        String body,
        int participantCount,
        String changedByName,
        LocalDateTime changedDttm,
        String changeReason
) {
}
