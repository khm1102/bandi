package kr.ac.tukorea.bandi.domain.resource.dto.request;

import kr.ac.tukorea.bandi.domain.resource.model.ResourceTargetScope;

public record ResourceReadableSearchCondition(
        String keyword,
        String categoryCode,
        ResourceTargetScope targetScope,
        Long memberTeamId,
        boolean admin,
        int offset,
        int limit
) {

    public ResourceReadableSearchCondition(String keyword, String categoryCode,
                                           Long memberTeamId, boolean admin,
                                           int offset, int limit) {
        this(keyword, categoryCode, null, memberTeamId, admin, offset, limit);
    }

    public static ResourceReadableSearchCondition from(
            ResourceSearchParam param, Long memberTeamId, boolean admin) {
        return new ResourceReadableSearchCondition(normalize(param.keyword()),
                normalize(param.categoryCode()), param.targetScope(), memberTeamId, admin,
                param.page() * param.pageSize(), param.pageSize());
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
