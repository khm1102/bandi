package kr.ac.tukorea.bandi.domain.performance.dto.response;

public record PerformanceViewingGuideResponse(
        Long performanceViewingGuideId,
        Long performanceProjectId,
        String entryPolicy,
        String lateEntryPolicy,
        String recordingPolicy,
        String cancellationPolicy,
        String accessibilityPolicy,
        String directions,
        String parkingInformation
) {
}
