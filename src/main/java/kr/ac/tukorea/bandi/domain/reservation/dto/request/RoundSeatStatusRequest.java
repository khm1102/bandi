package kr.ac.tukorea.bandi.domain.reservation.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import kr.ac.tukorea.bandi.domain.reservation.model.RoundSeatStatus;

public record RoundSeatStatusRequest(
        @NotNull @Positive Long performanceProjectId,
        @NotNull RoundSeatStatus status
) {
}
