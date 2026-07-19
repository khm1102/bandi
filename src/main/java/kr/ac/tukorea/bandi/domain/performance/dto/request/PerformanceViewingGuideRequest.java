package kr.ac.tukorea.bandi.domain.performance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PerformanceViewingGuideRequest(
        @NotNull @Positive Long performanceProjectId,
        @NotBlank String entryPolicy,
        @NotBlank String lateEntryPolicy,
        @NotBlank String recordingPolicy,
        @NotBlank String cancellationPolicy,
        @NotBlank String accessibilityPolicy,
        @NotBlank String directions,
        String parkingInformation
) {
    public PerformanceViewingGuideWriteParam toParam() {
        return new PerformanceViewingGuideWriteParam(performanceProjectId,
                entryPolicy, lateEntryPolicy, recordingPolicy,
                cancellationPolicy, accessibilityPolicy, directions,
                parkingInformation);
    }
}
