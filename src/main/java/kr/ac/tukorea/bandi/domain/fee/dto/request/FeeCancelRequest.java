package kr.ac.tukorea.bandi.domain.fee.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FeeCancelRequest(
        @NotBlank @Size(max = 500) String reason
) {
}
