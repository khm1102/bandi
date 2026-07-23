package kr.ac.tukorea.bandi.domain.activity.dto.response;

import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecordStatus;

public record ActivityReportDocumentSavedResponse(
        Long activityRecordId,
        Long documentStoredFileId,
        String filename,
        ActivityRecordStatus status
) {
}
