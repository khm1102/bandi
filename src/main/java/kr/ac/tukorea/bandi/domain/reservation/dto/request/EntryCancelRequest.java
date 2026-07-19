package kr.ac.tukorea.bandi.domain.reservation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record EntryCancelRequest(
        @NotNull @Positive Long reservationSeatId,
        @NotBlank @Size(max = 500) String reason
) {
}
