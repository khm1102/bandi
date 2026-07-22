package kr.ac.tukorea.bandi.domain.notice.dto.request;

import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeStatus;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeTargetScope;

public record InternalNoticeManageSearchCondition(
        String keyword,
        InternalNoticeStatus status,
        InternalNoticeTargetScope targetScope,
        Long teamId,
        int offset,
        int limit
) {

    public static InternalNoticeManageSearchCondition forAdmin(
            InternalNoticeManageSearchParam param) {
        return new InternalNoticeManageSearchCondition(normalize(param.keyword()),
                param.status(), param.targetScope(), param.teamId(),
                param.page() * param.pageSize(), param.pageSize());
    }

    public static InternalNoticeManageSearchCondition forLeader(
            InternalNoticeManageSearchParam param, Long teamId) {
        return new InternalNoticeManageSearchCondition(normalize(param.keyword()),
                param.status(), InternalNoticeTargetScope.TEAM, teamId,
                param.page() * param.pageSize(), param.pageSize());
    }

    private static String normalize(String keyword) {
        return keyword == null || keyword.isBlank() ? null : keyword.strip();
    }
}
