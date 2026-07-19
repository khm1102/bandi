package kr.ac.tukorea.bandi.domain.performance.model;

import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPerformanceViewingGuideException;
import lombok.Getter;

@Getter
public class PerformanceViewingGuide {

    private Long performanceViewingGuideId;
    private final Long performanceProjectId;
    private final String entryPolicy;
    private final String lateEntryPolicy;
    private final String recordingPolicy;
    private final String cancellationPolicy;
    private final String accessibilityPolicy;
    private final String directions;
    private final String parkingInformation;

    public PerformanceViewingGuide(
            Long performanceViewingGuideId, Long performanceProjectId,
            String entryPolicy, String lateEntryPolicy,
            String recordingPolicy, String cancellationPolicy,
            String accessibilityPolicy, String directions,
            String parkingInformation) {
        this.performanceViewingGuideId = performanceViewingGuideId;
        this.performanceProjectId = requireId(performanceProjectId);
        this.entryPolicy = requireText(entryPolicy, "entryPolicy");
        this.lateEntryPolicy = requireText(
                lateEntryPolicy, "lateEntryPolicy");
        this.recordingPolicy = requireText(
                recordingPolicy, "recordingPolicy");
        this.cancellationPolicy = requireText(
                cancellationPolicy, "cancellationPolicy");
        this.accessibilityPolicy = requireText(
                accessibilityPolicy, "accessibilityPolicy");
        this.directions = optionalText(directions);
        this.parkingInformation = optionalText(parkingInformation);
    }

    public static PerformanceViewingGuide create(
            Long performanceProjectId, String entryPolicy,
            String lateEntryPolicy, String recordingPolicy,
            String cancellationPolicy, String accessibilityPolicy,
            String directions, String parkingInformation) {
        return new PerformanceViewingGuide(null, performanceProjectId,
                entryPolicy, lateEntryPolicy, recordingPolicy,
                cancellationPolicy, accessibilityPolicy, directions,
                parkingInformation);
    }

    public PerformanceViewingGuide edit(
            String entryPolicy, String lateEntryPolicy,
            String recordingPolicy, String cancellationPolicy,
            String accessibilityPolicy, String directions,
            String parkingInformation) {
        return new PerformanceViewingGuide(performanceViewingGuideId,
                performanceProjectId, entryPolicy, lateEntryPolicy,
                recordingPolicy, cancellationPolicy, accessibilityPolicy,
                directions, parkingInformation);
    }

    private static Long requireId(Long value) {
        if (value == null || value < 1) {
            throw new InvalidPerformanceViewingGuideException(
                    "performanceProjectId");
        }
        return value;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new InvalidPerformanceViewingGuideException(field);
        }
        return value;
    }

    private static String optionalText(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
