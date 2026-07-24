package kr.ac.tukorea.bandi.domain.activity.model;

import kr.ac.tukorea.bandi.domain.activity.exception.InvalidActivityReportDocumentException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ActivityReportDocumentRecord {

    private Long activityReportDocumentId;
    private final Long activityRecordId;
    private final String representative;
    private final String location;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;

    public ActivityReportDocumentRecord(Long activityReportDocumentId,
                                        Long activityRecordId,
                                        String representative,
                                        String location,
                                        LocalDateTime createdDttm,
                                        LocalDateTime updatedDttm) {
        if (activityRecordId == null || representative == null
                || representative.isBlank() || representative.length() > 20
                || location == null || location.isBlank() || location.length() > 50) {
            throw new InvalidActivityReportDocumentException("record");
        }
        this.activityReportDocumentId = activityReportDocumentId;
        this.activityRecordId = activityRecordId;
        this.representative = representative.strip();
        this.location = location.strip();
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
    }

    public static ActivityReportDocumentRecord create(Long activityRecordId,
                                                       ActivityReportDocument document) {
        if (document == null) {
            throw new InvalidActivityReportDocumentException("document");
        }
        return new ActivityReportDocumentRecord(null, activityRecordId,
                document.representative(), document.location(), null, null);
    }
}
