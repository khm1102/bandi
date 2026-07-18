package kr.ac.tukorea.bandi.domain.event.dto.response;

import kr.ac.tukorea.bandi.domain.event.model.AttendanceStatus;

public record AttendanceStatusCountResponse(
        AttendanceStatus status,
        int count
) {
}
