package kr.ac.tukorea.bandi.domain.performance.dto.request;

import java.time.LocalDateTime;

public record PerformancePublicPageWriteParam(
        Long performancePublicPageId,
        Long performanceProjectId,
        String slug,
        String shortDescription,
        String synopsis,
        String directorNote,
        String genre,
        String ageRating,
        int runtimeMinutes,
        Integer intermissionMinutes,
        long admissionFee,
        Long heroFileId,
        Long posterFileId,
        String accentColor,
        String contactName,
        String contactChannel,
        String organizerName,
        String ogTitle,
        String ogDescription,
        Long ogImageFileId,
        LocalDateTime publishStartDttm,
        LocalDateTime publishEndDttm
) {
}
