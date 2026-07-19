package kr.ac.tukorea.bandi.domain.event.dto.response;

import kr.ac.tukorea.bandi.domain.event.model.AttendanceStatus;

import java.time.LocalDateTime;

public record EventAttendanceResponse(
        Long eventAttendanceId,
        Long memberId,
        String memberName,
        Long teamId,
        String teamName,
        AttendanceStatus status,
        String processedByName,
        LocalDateTime processedDttm,
        String reason
) {
}
