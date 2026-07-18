package kr.ac.tukorea.bandi.domain.activity.model;

import kr.ac.tukorea.bandi.domain.activity.exception.InvalidActivityRecordException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ActivityRecordRevision {

    private Long activityRecordRevisionId;
    private final Long activityRecordId;
    private final int revisionNo;
    private final LocalDateTime activityDttm;
    private final String title;
    private final String body;
    private final int participantCount;
    private final Long changedByMemberId;
    private final LocalDateTime changedDttm;
    private final String changeReason;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;

    public ActivityRecordRevision(Long activityRecordRevisionId,
                                  Long activityRecordId, int revisionNo,
                                  LocalDateTime activityDttm, String title,
                                  String body, int participantCount,
                                  Long changedByMemberId,
                                  LocalDateTime changedDttm,
                                  String changeReason,
                                  LocalDateTime createdDttm,
                                  LocalDateTime updatedDttm) {
        if (activityRecordId == null || revisionNo < 1 || activityDttm == null
                || title == null || title.isBlank() || body == null || body.isBlank()
                || participantCount < 1 || changedByMemberId == null
                || changedDttm == null) {
            throw new InvalidActivityRecordException("revision");
        }
        this.activityRecordRevisionId = activityRecordRevisionId;
        this.activityRecordId = activityRecordId;
        this.revisionNo = revisionNo;
        this.activityDttm = activityDttm;
        this.title = title;
        this.body = body;
        this.participantCount = participantCount;
        this.changedByMemberId = changedByMemberId;
        this.changedDttm = changedDttm;
        this.changeReason = normalize(changeReason);
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
    }

    public static ActivityRecordRevision snapshot(ActivityRecord record,
                                                  int revisionNo,
                                                  Long changedByMemberId,
                                                  LocalDateTime changedDttm,
                                                  String changeReason) {
        if (record == null) {
            throw new InvalidActivityRecordException("revision-record");
        }
        return new ActivityRecordRevision(null, record.getActivityRecordId(),
                revisionNo, record.getActivityDttm(), record.getTitle(),
                record.getBody(), record.getParticipantCount(),
                changedByMemberId, changedDttm, changeReason, null, null);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
