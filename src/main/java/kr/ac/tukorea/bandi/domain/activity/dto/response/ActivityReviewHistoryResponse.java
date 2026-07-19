package kr.ac.tukorea.bandi.domain.activity.dto.response;

import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecordStatus;

import java.time.LocalDateTime;

public record ActivityReviewHistoryResponse(
        ActivityRecordStatus previousStatus,
        ActivityRecordStatus newStatus,
        String comment,
        String reviewedByName,
        LocalDateTime reviewedDttm
) {
}
