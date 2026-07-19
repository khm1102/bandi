package kr.ac.tukorea.bandi.domain.resource.dto.request;

public record ResourceReadableSearchCondition(
        String keyword,
        String categoryCode,
        Long memberTeamId,
        boolean admin,
        int offset,
        int limit
) {

    public static ResourceReadableSearchCondition from(
            ResourceSearchParam param, Long memberTeamId, boolean admin) {
        return new ResourceReadableSearchCondition(normalize(param.keyword()),
                normalize(param.categoryCode()), memberTeamId, admin,
                param.page() * param.pageSize(), param.pageSize());
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
