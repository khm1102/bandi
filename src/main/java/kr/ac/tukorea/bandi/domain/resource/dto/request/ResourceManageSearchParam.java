package kr.ac.tukorea.bandi.domain.resource.dto.request;

import kr.ac.tukorea.bandi.domain.resource.exception.InvalidResourceException;
import kr.ac.tukorea.bandi.domain.resource.model.ResourceStatus;
import kr.ac.tukorea.bandi.domain.resource.model.ResourceTargetScope;

public record ResourceManageSearchParam(
        String keyword,
        String categoryCode,
        ResourceStatus status,
        ResourceTargetScope targetScope,
        Long teamId,
        int page,
        int pageSize
) {

    private static final int MAX_KEYWORD_LENGTH = 200;
    private static final int MAX_CATEGORY_LENGTH = 30;
    private static final int MAX_PAGE_SIZE = 100;

    public ResourceManageSearchParam {
        validatePage(page, pageSize);
        validateLength(keyword, MAX_KEYWORD_LENGTH, "keyword");
        validateLength(categoryCode, MAX_CATEGORY_LENGTH, "category");
        if (teamId != null && targetScope != ResourceTargetScope.TEAM) {
            throw new InvalidResourceException("team-filter");
        }
    }

    private static void validatePage(int page, int pageSize) {
        if (page < 0 || pageSize < 1 || pageSize > MAX_PAGE_SIZE
                || page > Integer.MAX_VALUE / pageSize) {
            throw new InvalidResourceException("page");
        }
    }

    private static void validateLength(String value, int maxLength, String field) {
        if (value != null && value.length() > maxLength) {
            throw new InvalidResourceException(field);
        }
    }
}
