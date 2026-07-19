package kr.ac.tukorea.bandi.domain.event.dto.request;

import kr.ac.tukorea.bandi.domain.event.model.AttendanceStatus;

public record AttendanceRosterFilter(AttendanceStatus status) {
}
