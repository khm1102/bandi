package kr.ac.tukorea.bandi.domain.resource.dto.request;

import kr.ac.tukorea.bandi.domain.resource.model.ResourceTargetScope;

public record ResourceReadFilter(
        ResourceTargetScope targetScope
) {
}
