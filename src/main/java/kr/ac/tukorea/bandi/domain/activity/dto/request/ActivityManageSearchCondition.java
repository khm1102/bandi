package kr.ac.tukorea.bandi.domain.activity.dto.request;

import kr.ac.tukorea.bandi.domain.activity.model.ActivityRecordStatus;

public record ActivityManageSearchCondition(
        Long teamId,
        ActivityRecordStatus status,
        Long createdByMemberId,
        int offset,
        int limit
) {

    public static ActivityManageSearchCondition forAdmin(ActivityManageSearchParam param) {
        return new ActivityManageSearchCondition(param.teamId(), param.status(),
                param.createdByMemberId(), param.page() * param.pageSize(),
                param.pageSize());
    }

    public static ActivityManageSearchCondition forLeader(
            ActivityManageSearchParam param, Long teamId) {
        return new ActivityManageSearchCondition(teamId, param.status(),
                param.createdByMemberId(), param.page() * param.pageSize(),
                param.pageSize());
    }

    public static ActivityManageSearchCondition forMember(
            ActivityManageSearchParam param, Long memberId) {
        return new ActivityManageSearchCondition(null, param.status(), memberId,
                param.page() * param.pageSize(), param.pageSize());
    }
}
