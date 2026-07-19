package kr.ac.tukorea.bandi.domain.event.dto.response;

import kr.ac.tukorea.bandi.domain.event.model.AttendanceStatus;

import java.time.LocalDateTime;

public record MemberAttendanceResponse(
        Long clubEventId,
        String eventTitle,
        LocalDateTime eventStartDttm,
        AttendanceStatus status,
        LocalDateTime processedDttm,
        String reason
) {
}
