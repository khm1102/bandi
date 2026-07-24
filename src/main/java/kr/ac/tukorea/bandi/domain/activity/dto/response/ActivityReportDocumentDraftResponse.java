package kr.ac.tukorea.bandi.domain.activity.dto.response;

import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecordStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ActivityReportDocumentDraftResponse(
        Long activityRecordId,
        String title,
        String representative,
        String location,
        LocalDateTime activityAt,
        String content,
        List<ActivityReportParticipantResponse> participants,
        ActivityRecordStatus status,
        Long photoStoredFileId,
        String photoOriginalName,
        Long documentStoredFileId,
        String documentOriginalName
) {
}
