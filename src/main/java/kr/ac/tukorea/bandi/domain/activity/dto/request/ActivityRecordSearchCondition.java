package kr.ac.tukorea.bandi.domain.activity.dto.request;

import java.time.LocalDateTime;

public record ActivityRecordSearchCondition(
        Long teamId,
        LocalDateTime rangeStart,
        LocalDateTime rangeEnd,
        int offset,
        int limit
) {

    public static ActivityRecordSearchCondition from(ActivityRecordSearchParam param) {
        LocalDateTime start = param.dateFrom() == null
                ? null : param.dateFrom().atStartOfDay();
        LocalDateTime end = param.dateTo() == null
                ? null : param.dateTo().plusDays(1).atStartOfDay();
        return new ActivityRecordSearchCondition(param.teamId(), start, end,
                param.page() * param.pageSize(), param.pageSize());
    }
}
