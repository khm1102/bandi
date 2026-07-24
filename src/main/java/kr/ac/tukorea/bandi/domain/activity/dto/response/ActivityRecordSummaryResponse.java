package kr.ac.tukorea.bandi.domain.activity.dto.response;

import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecordStatus;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecordType;

import java.time.LocalDateTime;

public record ActivityRecordSummaryResponse(
        Long activityRecordId,
        Long teamId,
        String teamName,
        LocalDateTime activityDttm,
        String title,
        int participantCount,
        ActivityRecordStatus status,
        ActivityRecordType recordType,
        String createdByName,
        Long representativeStoredFileId,
        Long documentStoredFileId,
        LocalDateTime updatedDttm
) {
}
