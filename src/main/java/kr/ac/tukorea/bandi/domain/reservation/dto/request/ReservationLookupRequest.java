package kr.ac.tukorea.bandi.domain.reservation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReservationLookupRequest(
        @NotBlank @Size(max = 200) String lookupToken
) {
}
