package kr.ac.tukorea.bandi.domain.event.model;

import kr.ac.tukorea.bandi.domain.event.exception.InvalidEventAttendanceException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class EventAttendanceHistory {

    private static final int MAX_REASON_LENGTH = 500;

    private Long eventAttendanceHistoryId;
    private final Long eventAttendanceId;
    private final AttendanceStatus previousStatus;
    private final AttendanceStatus newStatus;
    private final String reason;
    private final Long changedByMemberId;
    private final LocalDateTime changedDttm;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;

    public EventAttendanceHistory(Long eventAttendanceHistoryId,
                                  Long eventAttendanceId,
                                  AttendanceStatus previousStatus,
                                  AttendanceStatus newStatus, String reason,
                                  Long changedByMemberId,
                                  LocalDateTime changedDttm,
                                  LocalDateTime createdDttm,
                                  LocalDateTime updatedDttm) {
        String normalizedReason = normalize(reason);
        if (eventAttendanceId == null || previousStatus == null
                || newStatus == null || newStatus == AttendanceStatus.PENDING
                || previousStatus == newStatus
                || changedByMemberId == null || changedDttm == null
                || (newStatus == AttendanceStatus.EXCUSED
                && normalizedReason == null)
                || (normalizedReason != null
                && normalizedReason.length() > MAX_REASON_LENGTH)) {
            throw new InvalidEventAttendanceException("history");
        }
        this.eventAttendanceHistoryId = eventAttendanceHistoryId;
        this.eventAttendanceId = eventAttendanceId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.reason = normalizedReason;
        this.changedByMemberId = changedByMemberId;
        this.changedDttm = changedDttm;
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
    }

    public static EventAttendanceHistory change(Long eventAttendanceId,
                                                AttendanceStatus previousStatus,
                                                AttendanceStatus newStatus,
                                                String reason,
                                                Long actorMemberId,
                                                LocalDateTime currentDttm) {
        return new EventAttendanceHistory(null, eventAttendanceId,
                previousStatus, newStatus, reason, actorMemberId,
                currentDttm, null, null);
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
