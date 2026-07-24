package kr.ac.tukorea.bandi.domain.activity.dto.request;

import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecordStatus;
import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecordType;

import java.time.LocalDateTime;

public record ActivityRecordListSearchCondition(
        String keyword,
        Long teamId,
        Long createdByMemberId,
        ActivityRecordStatus status,
        ActivityRecordType recordType,
        LocalDateTime rangeStart,
        LocalDateTime rangeEnd,
        int offset,
        int limit
) {

    public static ActivityRecordListSearchCondition forMine(
            ActivityRecordListSearchParam param, Long memberId) {
        return create(param, null, memberId, param.status());
    }

    public static ActivityRecordListSearchCondition forArchive(
            ActivityRecordListSearchParam param) {
        return create(param, param.teamId(), null, param.status());
    }

    public static ActivityRecordListSearchCondition forReview(
            ActivityRecordListSearchParam param, Long enforcedTeamId) {
        return create(param, enforcedTeamId == null ? param.teamId() : enforcedTeamId,
                null, param.status());
    }

    private static ActivityRecordListSearchCondition create(
            ActivityRecordListSearchParam param, Long teamId, Long createdByMemberId,
            ActivityRecordStatus status) {
        String keyword = param.keyword() == null || param.keyword().isBlank()
                ? null : param.keyword().strip();
        LocalDateTime rangeStart = param.dateFrom() == null
                ? null : param.dateFrom().atStartOfDay();
        LocalDateTime rangeEnd = param.dateTo() == null
                ? null : param.dateTo().plusDays(1).atStartOfDay();
        return new ActivityRecordListSearchCondition(keyword, teamId,
                createdByMemberId, status, param.recordType(), rangeStart,
                rangeEnd, param.page() * param.pageSize(), param.pageSize());
    }
}
