package kr.ac.tukorea.bandi.domain.activity.dto.request;

import kr.ac.tukorea.bandi.domain.activity.exception.InvalidActivityRecordException;

import java.time.LocalDate;

public record ActivityRecordSearchParam(
        Long teamId,
        LocalDate dateFrom,
        LocalDate dateTo,
        int page,
        int pageSize
) {

    private static final int MAX_PAGE_SIZE = 100;

    public ActivityRecordSearchParam {
        if (page < 0 || pageSize < 1 || pageSize > MAX_PAGE_SIZE
                || page > Integer.MAX_VALUE / pageSize
                || (dateFrom != null && dateTo != null && dateTo.isBefore(dateFrom))) {
            throw new InvalidActivityRecordException("search");
        }
    }
}
