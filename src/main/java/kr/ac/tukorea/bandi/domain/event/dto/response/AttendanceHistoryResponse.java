package kr.ac.tukorea.bandi.domain.event.dto.response;

import kr.ac.tukorea.bandi.domain.event.model.AttendanceStatus;

import java.time.LocalDateTime;

public record AttendanceHistoryResponse(
        AttendanceStatus previousStatus,
        AttendanceStatus newStatus,
        String reason,
        String changedByName,
        LocalDateTime changedDttm
) {
}
