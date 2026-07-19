package kr.ac.tukorea.bandi.domain.activity.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ActivityRecordCreateRequest(
        @NotNull @Positive Long teamId,
        @NotNull LocalDateTime activityDttm,
        @NotBlank @Size(max = 150) String title,
        @NotBlank String body,
        @Positive int participantCount
) {

    public ActivityRecordWriteParam toParam() {
        return new ActivityRecordWriteParam(teamId, activityDttm, title, body,
                participantCount);
    }
}
