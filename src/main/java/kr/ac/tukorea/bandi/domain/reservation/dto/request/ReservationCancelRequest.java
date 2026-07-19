package kr.ac.tukorea.bandi.domain.reservation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReservationCancelRequest(
        @NotBlank @Size(max = 200) String lookupToken,
        @NotBlank @Size(max = 500) String reason
) {
}
