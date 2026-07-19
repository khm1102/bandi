package kr.ac.tukorea.bandi.domain.notice.dto.request;

import kr.ac.tukorea.bandi.domain.notice.exception.InvalidPublicNoticeException;

public record PublicNoticeSearchParam(
        String keyword,
        int page,
        int pageSize
) {

    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_KEYWORD_LENGTH = 200;

    public PublicNoticeSearchParam {
        if (page < 0 || pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw new InvalidPublicNoticeException("page");
        }
        if (page > Integer.MAX_VALUE / pageSize) {
            throw new InvalidPublicNoticeException("page-offset");
        }
        if (keyword != null && keyword.length() > MAX_KEYWORD_LENGTH) {
            throw new InvalidPublicNoticeException("keyword");
        }
    }
}
