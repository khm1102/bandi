package kr.ac.tukorea.bandi.domain.activity.dto.request;

import kr.ac.tukorea.bandi.domain.activity.exception.InvalidActivityRecordException;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecordStatus;

public record ActivityManageSearchParam(
        Long teamId,
        ActivityRecordStatus status,
        Long createdByMemberId,
        int page,
        int pageSize
) {

    private static final int MAX_PAGE_SIZE = 100;

    public ActivityManageSearchParam {
        if (page < 0 || pageSize < 1 || pageSize > MAX_PAGE_SIZE
                || page > Integer.MAX_VALUE / pageSize) {
            throw new InvalidActivityRecordException("page");
        }
    }
}
