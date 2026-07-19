package kr.ac.tukorea.bandi.domain.performance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import kr.ac.tukorea.bandi.domain.performance.model.MediaType;

public record PerformanceMediaRequest(
        @NotNull @Positive Long performanceProjectId,
        @NotNull @Positive Long storedFileId,
        @NotNull MediaType mediaType,
        @NotBlank @Size(max = 200) String title,
        @NotBlank String description,
        @NotBlank @Size(max = 500) String altText,
        @NotBlank @Size(max = 500) String creditText,
        @Size(max = 1000) String externalUrl,
        @PositiveOrZero int displayOrder
) {

    public PerformanceMediaWriteParam toParam(Long mediaId) {
        return new PerformanceMediaWriteParam(mediaId,
                performanceProjectId, storedFileId, mediaType, title,
                description, altText, creditText, externalUrl, displayOrder);
    }
}
