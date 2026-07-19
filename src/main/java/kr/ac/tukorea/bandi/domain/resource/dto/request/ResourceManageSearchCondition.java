package kr.ac.tukorea.bandi.domain.resource.dto.request;

import kr.ac.tukorea.bandi.domain.resource.model.ResourceStatus;
import kr.ac.tukorea.bandi.domain.resource.model.ResourceTargetScope;

public record ResourceManageSearchCondition(
        String keyword,
        String categoryCode,
        ResourceStatus status,
        ResourceTargetScope targetScope,
        Long teamId,
        int offset,
        int limit
) {

    public static ResourceManageSearchCondition forAdmin(ResourceManageSearchParam param) {
        return new ResourceManageSearchCondition(normalize(param.keyword()),
                normalize(param.categoryCode()), param.status(), param.targetScope(),
                param.teamId(), param.page() * param.pageSize(), param.pageSize());
    }

    public static ResourceManageSearchCondition forLeader(
            ResourceManageSearchParam param, Long teamId) {
        return new ResourceManageSearchCondition(normalize(param.keyword()),
                normalize(param.categoryCode()), param.status(),
                ResourceTargetScope.TEAM, teamId,
                param.page() * param.pageSize(), param.pageSize());
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
