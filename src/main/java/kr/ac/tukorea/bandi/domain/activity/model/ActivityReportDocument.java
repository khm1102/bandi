package kr.ac.tukorea.bandi.domain.activity.model;

import kr.ac.tukorea.bandi.domain.activity.exception.InvalidActivityReportDocumentException;

import java.time.LocalDateTime;
import java.util.List;

public record ActivityReportDocument(
        String representative,
        String location,
        LocalDateTime activityAt,
        String content,
        List<ActivityReportParticipant> participants
) {

    private static final int REPRESENTATIVE_MAX_LENGTH = 20;
    private static final int LOCATION_MAX_LENGTH = 50;
    private static final int CONTENT_MAX_LENGTH = 300;
    private static final int PARTICIPANT_MAX_COUNT = 14;

    public static ActivityReportDocument create(
            String representative,
            String location,
            LocalDateTime activityAt,
            String content,
            List<ActivityReportParticipant> participants
    ) {
        validateRequired(representative, REPRESENTATIVE_MAX_LENGTH, "representative");
        validateRequired(location, LOCATION_MAX_LENGTH, "location");
        if (activityAt == null) {
            throw new InvalidActivityReportDocumentException("activityAt");
        }
        validateRequired(content, CONTENT_MAX_LENGTH, "content");
        if (participants == null || participants.isEmpty()
                || participants.size() > PARTICIPANT_MAX_COUNT
                || participants.stream().anyMatch(java.util.Objects::isNull)) {
            throw new InvalidActivityReportDocumentException("participants");
        }
        return new ActivityReportDocument(representative, location, activityAt,
                content, List.copyOf(participants));
    }

    private static void validateRequired(String value, int maxLength, String field) {
        if (value == null || value.isBlank() || value.length() > maxLength) {
            throw new InvalidActivityReportDocumentException(field);
        }
    }
}
