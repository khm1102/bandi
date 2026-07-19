package kr.ac.tukorea.bandi.domain.policy.dto.request;

import jakarta.validation.constraints.NotNull;

public record PolicyActiveRequest(
        @NotNull Boolean active
) {
}
