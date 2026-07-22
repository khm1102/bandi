package kr.ac.tukorea.bandi.domain.activity.dto.request;

import jakarta.validation.constraints.Size;

public record ActivitySubmitRequest(
        @Size(max = 500) String changeReason
) {
}
