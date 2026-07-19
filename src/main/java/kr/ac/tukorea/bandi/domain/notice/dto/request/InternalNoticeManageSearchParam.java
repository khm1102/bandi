package kr.ac.tukorea.bandi.domain.notice.dto.request;

import kr.ac.tukorea.bandi.domain.notice.exception.InvalidInternalNoticeException;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeStatus;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeTargetScope;

public record InternalNoticeManageSearchParam(
        String keyword,
        InternalNoticeStatus status,
        InternalNoticeTargetScope targetScope,
        Long teamId,
        int page,
        int pageSize
) {

    private static final int MAX_KEYWORD_LENGTH = 200;
    private static final int MAX_PAGE_SIZE = 100;

    public InternalNoticeManageSearchParam {
        if (page < 0 || pageSize < 1 || pageSize > MAX_PAGE_SIZE
                || page > Integer.MAX_VALUE / pageSize) {
            throw new InvalidInternalNoticeException("page");
        }
        if (keyword != null && keyword.length() > MAX_KEYWORD_LENGTH) {
            throw new InvalidInternalNoticeException("keyword");
        }
        if (teamId != null && targetScope != InternalNoticeTargetScope.TEAM) {
            throw new InvalidInternalNoticeException("team-filter");
        }
    }
}
