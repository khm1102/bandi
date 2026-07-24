package kr.ac.tukorea.bandi.domain.activity.dto.request;

import kr.ac.tukorea.bandi.domain.activity.exception.InvalidActivityRecordException;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecordStatus;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecordType;

import java.time.LocalDate;

public record ActivityRecordListSearchParam(
        String keyword,
        Long teamId,
        ActivityRecordStatus status,
        ActivityRecordType recordType,
        LocalDate dateFrom,
        LocalDate dateTo,
        Integer page,
        Integer pageSize
) {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;

    public ActivityRecordListSearchParam {
        if (page == null) {
            page = 0;
        }
        if (pageSize == null || pageSize == 0) {
            pageSize = DEFAULT_PAGE_SIZE;
        }
        if (page < 0 || pageSize < 1 || pageSize > MAX_PAGE_SIZE
                || page > Integer.MAX_VALUE / pageSize
                || (dateFrom != null && dateTo != null && dateTo.isBefore(dateFrom))) {
            throw new InvalidActivityRecordException("search");
        }
    }
}
