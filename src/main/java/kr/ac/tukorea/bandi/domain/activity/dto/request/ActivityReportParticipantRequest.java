package kr.ac.tukorea.bandi.domain.activity.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityReportParticipant;

public record ActivityReportParticipantRequest(
        @NotBlank(message = "{activity.report.participant.name.required}")
        @Size(max = 20, message = "{activity.report.participant.name.size}")
        String name,
        @Size(max = 30, message = "{activity.report.participant.department.size}")
        String department,
        @Size(max = 20, message = "{activity.report.participant.studentNo.size}")
        String studentNo,
        @Size(max = 40, message = "{activity.report.participant.note.size}")
        String note
) {

    public ActivityReportParticipant toModel() {
        return new ActivityReportParticipant(name, department, studentNo, note);
    }
}
