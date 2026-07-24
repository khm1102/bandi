package kr.ac.tukorea.bandi.domain.activity.dto.response;

import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecordStatus;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecordType;

import java.time.LocalDateTime;

public record ActivityReviewCsvRow(
        String title,
        ActivityRecordType recordType,
        String teamName,
        String createdByName,
        LocalDateTime activityDttm,
        int participantCount,
        ActivityRecordStatus status,
        String latestReviewerName,
        String latestReviewComment,
        boolean reportDocument
) {
}
