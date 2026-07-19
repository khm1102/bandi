package kr.ac.tukorea.bandi.domain.reservation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EntryTokenRequest(
        @NotBlank @Size(max = 200) String entryToken
) {
}
