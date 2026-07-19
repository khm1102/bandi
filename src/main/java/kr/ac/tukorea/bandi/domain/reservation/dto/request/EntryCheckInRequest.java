package kr.ac.tukorea.bandi.domain.reservation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record EntryCheckInRequest(
        @NotBlank @Size(max = 200) String entryToken,
        @NotEmpty List<@NotNull @Positive Long> reservationSeatIds
) {
}
