package kr.ac.tukorea.bandi.domain.activity.dto.response;

import kr.ac.tukorea.bandi.domain.activity.model.ActivityReportParticipantRecord;

public record ActivityReportParticipantResponse(
        String name,
        String department,
        String studentNo,
        String note
) {

    public static ActivityReportParticipantResponse from(
            ActivityReportParticipantRecord participant) {
        return new ActivityReportParticipantResponse(participant.getName(),
                participant.getDepartment(), participant.getStudentNo(),
                participant.getNote());
    }
}
