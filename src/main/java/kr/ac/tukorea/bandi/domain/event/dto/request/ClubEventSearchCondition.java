package kr.ac.tukorea.bandi.domain.event.dto.request;

import kr.ac.tukorea.bandi.domain.event.exception.InvalidClubEventException;
import kr.ac.tukorea.bandi.domain.event.model.ClubEventStatus;

import java.time.LocalDateTime;

public record ClubEventSearchCondition(
        ClubEventStatus status,
        LocalDateTime rangeStart,
        LocalDateTime rangeEnd,
        int offset,
        int limit
) {

    private static final int MAX_LIMIT = 100;

    public ClubEventSearchCondition {
        if (offset < 0 || limit < 1 || limit > MAX_LIMIT
                || (rangeStart != null && rangeEnd != null
                && !rangeEnd.isAfter(rangeStart))) {
            throw new InvalidClubEventException("search-condition");
        }
    }
}
