package kr.ac.tukorea.bandi.domain.performance.dto.request;

import kr.ac.tukorea.bandi.domain.performance.model.PublicPageStatus;

public record PerformancePublicPageStatusParam(
        Long performancePublicPageId,
        PublicPageStatus status
) {
}
