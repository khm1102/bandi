package kr.ac.tukorea.bandi.domain.performance.dto.request;

import jakarta.validation.constraints.NotNull;

public record PerformancePublicNoticeRequest(
        @NotNull Long publicNoticeId
) {
}
