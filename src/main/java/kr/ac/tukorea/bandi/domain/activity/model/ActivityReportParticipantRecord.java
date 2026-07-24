package kr.ac.tukorea.bandi.domain.activity.model;

import kr.ac.tukorea.bandi.domain.activity.exception.InvalidActivityReportDocumentException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ActivityReportParticipantRecord {

    private Long activityReportParticipantId;
    private final Long activityReportDocumentId;
    private final int displayOrder;
    private final String name;
    private final String department;
    private final String studentNo;
    private final String note;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;

    public ActivityReportParticipantRecord(Long activityReportParticipantId,
                                           Long activityReportDocumentId,
                                           int displayOrder,
                                           String name,
                                           String department,
                                           String studentNo,
                                           String note,
                                           LocalDateTime createdDttm,
                                           LocalDateTime updatedDttm) {
        if (activityReportDocumentId == null || displayOrder < 0 || displayOrder > 13
                || name == null || name.isBlank() || name.length() > 20
                || tooLong(department, 30) || tooLong(studentNo, 20)
                || tooLong(note, 40)) {
            throw new InvalidActivityReportDocumentException("participant");
        }
        this.activityReportParticipantId = activityReportParticipantId;
        this.activityReportDocumentId = activityReportDocumentId;
        this.displayOrder = displayOrder;
        this.name = name.strip();
        this.department = normalize(department);
        this.studentNo = normalize(studentNo);
        this.note = normalize(note);
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
    }

    public static ActivityReportParticipantRecord create(Long documentId,
                                                          int displayOrder,
                                                          ActivityReportParticipant participant) {
        if (participant == null) {
            throw new InvalidActivityReportDocumentException("participant");
        }
        return new ActivityReportParticipantRecord(null, documentId, displayOrder,
                participant.name(), participant.department(), participant.studentNo(),
                participant.note(), null, null);
    }

    private static boolean tooLong(String value, int maxLength) {
        return value != null && value.length() > maxLength;
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
