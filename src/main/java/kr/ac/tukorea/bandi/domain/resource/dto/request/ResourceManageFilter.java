package kr.ac.tukorea.bandi.domain.resource.dto.request;

import kr.ac.tukorea.bandi.domain.resource.model.ResourceStatus;
import kr.ac.tukorea.bandi.domain.resource.model.ResourceTargetScope;

public record ResourceManageFilter(
        ResourceStatus status,
        ResourceTargetScope targetScope
) {
}
