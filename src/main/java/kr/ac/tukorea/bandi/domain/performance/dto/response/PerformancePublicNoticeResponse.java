package kr.ac.tukorea.bandi.domain.performance.dto.response;

import java.time.LocalDateTime;

public record PerformancePublicNoticeResponse(
        Long publicNoticeId,
        String categoryCode,
        String title,
        String body,
        boolean pinned,
        LocalDateTime publishStartDttm,
        LocalDateTime publishEndDttm,
        String createdByName,
        LocalDateTime updatedDttm
) {
}
