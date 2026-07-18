package kr.ac.tukorea.bandi.domain.event.model;

import kr.ac.tukorea.bandi.domain.event.exception.InvalidEventAttendanceException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class EventAttendance {

    private static final int MAX_REASON_LENGTH = 500;

    private Long eventAttendanceId;
    private final Long clubEventId;
    private final Long memberId;
    private final AttendanceStatus status;
    private final Long processedByMemberId;
    private final LocalDateTime processedDttm;
    private final String reason;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;

    public EventAttendance(Long eventAttendanceId, Long clubEventId,
                           Long memberId, AttendanceStatus status,
                           Long processedByMemberId,
                           LocalDateTime processedDttm, String reason,
                           LocalDateTime createdDttm,
                           LocalDateTime updatedDttm) {
        String normalizedReason = normalize(reason);
        validate(clubEventId, memberId, status, processedByMemberId,
                processedDttm, normalizedReason);
        this.eventAttendanceId = eventAttendanceId;
        this.clubEventId = clubEventId;
        this.memberId = memberId;
        this.status = status;
        this.processedByMemberId = processedByMemberId;
        this.processedDttm = processedDttm;
        this.reason = normalizedReason;
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
    }

    public static EventAttendance pending(Long clubEventId, Long memberId) {
        return new EventAttendance(null, clubEventId, memberId,
                AttendanceStatus.PENDING, null, null, null, null, null);
    }

    public EventAttendance changeStatus(AttendanceStatus newStatus,
                                        Long actorMemberId,
                                        LocalDateTime currentDttm,
                                        String changeReason) {
        if (newStatus == null || newStatus == AttendanceStatus.PENDING
                || newStatus == status) {
            throw new InvalidEventAttendanceException("status-change");
        }
        return new EventAttendance(eventAttendanceId, clubEventId, memberId,
                newStatus, actorMemberId, currentDttm, changeReason,
                createdDttm, updatedDttm);
    }

    private void validate(Long eventId, Long targetMemberId,
                          AttendanceStatus targetStatus, Long processorId,
                          LocalDateTime targetProcessedDttm,
                          String targetReason) {
        if (eventId == null || targetMemberId == null || targetStatus == null) {
            throw new InvalidEventAttendanceException("required");
        }
        boolean pending = targetStatus == AttendanceStatus.PENDING
                && processorId == null && targetProcessedDttm == null
                && targetReason == null;
        boolean processed = targetStatus != AttendanceStatus.PENDING
                && processorId != null && targetProcessedDttm != null;
        if (!pending && !processed) {
            throw new InvalidEventAttendanceException("processing-state");
        }
        if (targetStatus == AttendanceStatus.EXCUSED && targetReason == null) {
            throw new InvalidEventAttendanceException("excused-reason");
        }
        if (targetReason != null && targetReason.length() > MAX_REASON_LENGTH) {
            throw new InvalidEventAttendanceException("reason-length");
        }
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
