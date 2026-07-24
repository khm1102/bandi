package kr.ac.tukorea.bandi.domain.activity.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityReportDocument;

import java.time.LocalDateTime;
import java.util.List;

public record ActivityReportDocumentRequest(
        @NotBlank(message = "{activity.report.title.required}")
        @Size(max = 150, message = "{activity.report.title.size}")
        String title,
        @NotBlank(message = "{activity.report.representative.required}")
        @Size(max = 20, message = "{activity.report.representative.size}")
        String representative,
        @NotBlank(message = "{activity.report.location.required}")
        @Size(max = 50, message = "{activity.report.location.size}")
        String location,
        @NotNull(message = "{activity.report.activityAt.required}")
        LocalDateTime activityAt,
        @NotBlank(message = "{activity.report.content.required}")
        @Size(max = 300, message = "{activity.report.content.size}")
        String content,
        @NotNull(message = "{activity.report.participants.required}")
        @Size(min = 1, max = 14, message = "{activity.report.participants.size}")
        List<@Valid ActivityReportParticipantRequest> participants
) {

    public ActivityReportDocument toModel() {
        return ActivityReportDocument.create(title, representative, location, activityAt,
                content, participants.stream()
                        .map(ActivityReportParticipantRequest::toModel)
                        .toList());
    }
}
