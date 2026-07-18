package kr.ac.tukorea.bandi.domain.performance.dto.request;

import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPublicProfileException;
import kr.ac.tukorea.bandi.domain.performance.model.PublicProfileVisibility;

public record PublicProfileSearchCondition(
        Long memberId,
        PublicProfileVisibility visibilityStatus,
        int offset,
        int limit
) {

    public PublicProfileSearchCondition {
        if (offset < 0 || limit < 1 || limit > 100) {
            throw new InvalidPublicProfileException("pagination");
        }
    }
}
