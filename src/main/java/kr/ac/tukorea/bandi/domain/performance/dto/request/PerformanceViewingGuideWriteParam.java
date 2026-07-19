package kr.ac.tukorea.bandi.domain.performance.dto.request;

public record PerformanceViewingGuideWriteParam(
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
