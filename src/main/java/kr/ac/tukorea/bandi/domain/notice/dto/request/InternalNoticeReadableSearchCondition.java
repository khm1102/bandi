package kr.ac.tukorea.bandi.domain.notice.dto.request;

import java.time.LocalDateTime;

public record InternalNoticeReadableSearchCondition(
        String keyword,
        LocalDateTime currentDttm,
        Long memberId,
        Long memberTeamId,
        boolean admin,
        int offset,
        int limit
) {

    public static InternalNoticeReadableSearchCondition from(
            InternalNoticeSearchParam param, LocalDateTime currentDttm,
            Long memberId, Long memberTeamId, boolean admin) {
        String keyword = param.keyword() == null || param.keyword().isBlank()
                ? null
                : param.keyword().strip();
        return new InternalNoticeReadableSearchCondition(keyword, currentDttm,
                memberId, memberTeamId, admin, param.page() * param.pageSize(),
                param.pageSize());
    }
}
