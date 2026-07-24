package kr.ac.tukorea.bandi.domain.notice.dto.request;

import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeReadFilter;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeTargetScope;

import java.time.LocalDateTime;

public record InternalNoticeReadableSearchCondition(
        String keyword,
        LocalDateTime currentDttm,
        Long memberId,
        Long memberTeamId,
        boolean admin,
        InternalNoticeReadFilter readFilter,
        InternalNoticeTargetScope targetScope,
        int offset,
        int limit
) {

    public InternalNoticeReadableSearchCondition(String keyword, LocalDateTime currentDttm,
                                                 Long memberId, Long memberTeamId, boolean admin,
                                                 int offset, int limit) {
        this(keyword, currentDttm, memberId, memberTeamId, admin,
                InternalNoticeReadFilter.ALL, null, offset, limit);
    }

    public static InternalNoticeReadableSearchCondition from(
            InternalNoticeSearchParam param, LocalDateTime currentDttm,
            Long memberId, Long memberTeamId, boolean admin) {
        String keyword = param.keyword() == null || param.keyword().isBlank()
                ? null
                : param.keyword().strip();
        return new InternalNoticeReadableSearchCondition(keyword, currentDttm,
                memberId, memberTeamId, admin, param.readFilter(), param.targetScope(),
                param.page() * param.pageSize(),
                param.pageSize());
    }
}
