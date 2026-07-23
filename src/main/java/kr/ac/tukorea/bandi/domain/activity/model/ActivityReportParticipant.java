package kr.ac.tukorea.bandi.domain.activity.model;

import kr.ac.tukorea.bandi.domain.activity.exception.InvalidActivityReportDocumentException;

public record ActivityReportParticipant(
        String name,
        String department,
        String studentNo,
        String note
) {

    private static final int NAME_MAX_LENGTH = 20;
    private static final int DEPARTMENT_MAX_LENGTH = 30;
    private static final int STUDENT_NO_MAX_LENGTH = 20;
    private static final int NOTE_MAX_LENGTH = 40;

    public ActivityReportParticipant {
        validateRequired(name, NAME_MAX_LENGTH, "participant.name");
        validateOptional(department, DEPARTMENT_MAX_LENGTH, "participant.department");
        validateOptional(studentNo, STUDENT_NO_MAX_LENGTH, "participant.studentNo");
        validateOptional(note, NOTE_MAX_LENGTH, "participant.note");
    }

    private static void validateRequired(String value, int maxLength, String field) {
        if (value == null || value.isBlank() || value.length() > maxLength) {
            throw new InvalidActivityReportDocumentException(field);
        }
    }

    private static void validateOptional(String value, int maxLength, String field) {
        if (value != null && value.length() > maxLength) {
            throw new InvalidActivityReportDocumentException(field);
        }
    }
}
