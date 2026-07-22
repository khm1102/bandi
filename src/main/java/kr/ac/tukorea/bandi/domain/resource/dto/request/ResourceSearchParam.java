package kr.ac.tukorea.bandi.domain.resource.dto.request;

import kr.ac.tukorea.bandi.domain.resource.exception.InvalidResourceException;

public record ResourceSearchParam(
        String keyword,
        String categoryCode,
        int page,
        int pageSize
) {

    private static final int MAX_KEYWORD_LENGTH = 200;
    private static final int MAX_CATEGORY_LENGTH = 30;
    private static final int MAX_PAGE_SIZE = 100;

    public ResourceSearchParam {
        if (page < 0 || pageSize < 1 || pageSize > MAX_PAGE_SIZE
                || page > Integer.MAX_VALUE / pageSize) {
            throw new InvalidResourceException("page");
        }
        if (keyword != null && keyword.length() > MAX_KEYWORD_LENGTH) {
            throw new InvalidResourceException("keyword");
        }
        if (categoryCode != null && categoryCode.length() > MAX_CATEGORY_LENGTH) {
            throw new InvalidResourceException("category");
        }
    }
}
