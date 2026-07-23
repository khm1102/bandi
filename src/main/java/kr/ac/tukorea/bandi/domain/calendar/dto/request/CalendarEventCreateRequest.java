package kr.ac.tukorea.bandi.domain.calendar.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import kr.ac.tukorea.bandi.domain.calendar.model.CalendarEventColor;

import java.time.LocalDateTime;

public record CalendarEventCreateRequest(
        @Positive Long teamId,
        @NotBlank @Size(max = 150) String title,
        String description,
        @NotNull LocalDateTime startDttm,
        @NotNull LocalDateTime endDttm,
        boolean allDay,
        @Size(max = 200) String place,
        CalendarEventColor colorCode
) {

    public CalendarEventCreateParam toParam() {
        return new CalendarEventCreateParam(teamId, title, description,
                startDttm, endDttm, allDay, place, colorCode);
    }
}
