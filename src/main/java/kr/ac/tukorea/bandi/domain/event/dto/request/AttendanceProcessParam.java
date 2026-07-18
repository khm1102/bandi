package kr.ac.tukorea.bandi.domain.event.dto.request;

import kr.ac.tukorea.bandi.domain.event.model.AttendanceStatus;

import java.util.List;

public record AttendanceProcessParam(
        Long clubEventId,
        List<Long> eventAttendanceIds,
        AttendanceStatus status,
        String reason
) {
}
