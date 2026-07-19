package kr.ac.tukorea.bandi.domain.performance.dto.request;

import kr.ac.tukorea.bandi.domain.performance.model.MediaType;

public record PerformanceMediaWriteParam(
        Long performanceMediaId,
        Long performanceProjectId,
        Long storedFileId,
        MediaType mediaType,
        String title,
        String description,
        String altText,
        String creditText,
        String externalUrl,
        int displayOrder
) {
}
