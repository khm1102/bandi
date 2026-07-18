package kr.ac.tukorea.bandi.domain.performance.dto.response;

import kr.ac.tukorea.bandi.domain.performance.model.PublicPageStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PerformancePublicPageResponse(
        Long performancePublicPageId,
        Long performanceProjectId,
        String projectTitle,
        LocalDate productionStartDate,
        LocalDate productionEndDate,
        String place,
        String slug,
        PublicPageStatus status,
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
