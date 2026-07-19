package kr.ac.tukorea.bandi.domain.performance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record PerformancePublicPageRequest(
        @NotNull @Positive Long performanceProjectId,
        @NotBlank @Size(max = 100) String slug,
        @NotBlank @Size(max = 500) String shortDescription,
        @NotBlank String synopsis,
        String directorNote,
        @NotBlank @Size(max = 100) String genre,
        @NotBlank @Size(max = 50) String ageRating,
        @Positive int runtimeMinutes,
        @PositiveOrZero Integer intermissionMinutes,
        @PositiveOrZero long admissionFee,
        @Positive Long heroFileId,
        @Positive Long posterFileId,
        @Size(max = 20) String accentColor,
        @NotBlank @Size(max = 100) String contactName,
        @NotBlank @Size(max = 200) String contactChannel,
        @NotBlank @Size(max = 200) String organizerName,
        @Size(max = 200) String ogTitle,
        @Size(max = 500) String ogDescription,
        @Positive Long ogImageFileId,
        LocalDateTime publishStartDttm,
        LocalDateTime publishEndDttm
) {
    public PerformancePublicPageWriteParam toParam(Long pageId) {
        return new PerformancePublicPageWriteParam(pageId, performanceProjectId,
                slug, shortDescription, synopsis, directorNote, genre, ageRating,
                runtimeMinutes, intermissionMinutes, admissionFee, heroFileId,
                posterFileId, accentColor, contactName, contactChannel,
                organizerName, ogTitle, ogDescription, ogImageFileId,
                publishStartDttm, publishEndDttm);
    }
}
