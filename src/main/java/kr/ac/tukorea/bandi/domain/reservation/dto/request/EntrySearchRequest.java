package kr.ac.tukorea.bandi.domain.reservation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EntrySearchRequest(
        @NotBlank @Size(max = 30) String reservationNo,
        @NotBlank @Size(max = 100) String applicantName
) {
}
