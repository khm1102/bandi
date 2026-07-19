package kr.ac.tukorea.bandi.domain.activity.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActivityRevisionRequest(
        @NotBlank @Size(max = 1000) String comment
) {
}
