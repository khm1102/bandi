package kr.ac.tukorea.bandi.domain.event.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import kr.ac.tukorea.bandi.domain.event.model.AttendanceStatus;

import java.util.List;

public record AttendanceProcessRequest(
        @NotEmpty List<@Positive Long> eventAttendanceIds,
        @NotNull AttendanceStatus status,
        @Size(max = 500) String reason
) {

    public AttendanceProcessParam toParam(Long clubEventId) {
        return new AttendanceProcessParam(clubEventId, eventAttendanceIds,
                status, reason);
    }
}
