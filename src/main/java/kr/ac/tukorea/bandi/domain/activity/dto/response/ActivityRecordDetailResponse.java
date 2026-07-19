package kr.ac.tukorea.bandi.domain.activity.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ActivityRecordDetailResponse(
        Long activityRecordId,
        Long teamId,
        String teamName,
        LocalDateTime activityDttm,
        String title,
        String body,
        int participantCount,
        String createdByName,
        LocalDateTime updatedDttm,
        List<ActivityFileResponse> files
) {

    public static ActivityRecordDetailResponse of(
            ActivityRecordContentResponse content, List<ActivityFileResponse> files) {
        return new ActivityRecordDetailResponse(content.activityRecordId(),
                content.teamId(), content.teamName(), content.activityDttm(),
                content.title(), content.body(), content.participantCount(),
                content.createdByName(), content.updatedDttm(), List.copyOf(files));
    }
}
