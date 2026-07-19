package kr.ac.tukorea.bandi.domain.reservation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record RoundSeatRequest(
        @NotNull @Positive Long performanceProjectId,
        @NotNull @Positive Long performanceRoundId,
        @NotBlank @Size(max = 30) String seatLabel,
        @Size(max = 30) String sectionCode,
        @Size(max = 30) String rowLabel,
        @Size(max = 30) String columnLabel,
        @PositiveOrZero Integer displayRow,
        @PositiveOrZero Integer displayColumn,
        @Size(max = 30) String accessibilityCode
) {

    public RoundSeatCreateParam toParam() {
        return new RoundSeatCreateParam(performanceProjectId,
                performanceRoundId, seatLabel, sectionCode, rowLabel,
                columnLabel, displayRow, displayColumn, accessibilityCode);
    }
}
