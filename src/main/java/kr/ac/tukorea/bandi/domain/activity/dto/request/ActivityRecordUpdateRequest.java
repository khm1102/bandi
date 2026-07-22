package kr.ac.tukorea.bandi.domain.activity.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ActivityRecordUpdateRequest(
        @NotNull LocalDateTime activityDttm,
        @NotBlank @Size(max = 150) String title,
        @NotBlank String body,
        @Positive int participantCount
) {

    public ActivityRecordUpdateParam toParam(Long activityRecordId) {
        return new ActivityRecordUpdateParam(activityRecordId, activityDttm,
                title, body, participantCount);
    }
}
