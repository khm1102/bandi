package kr.ac.tukorea.bandi.domain.notice.dto.response;

import kr.ac.tukorea.bandi.domain.notice.model.PublicNoticeStatus;

import java.time.LocalDateTime;

public record PublicNoticeAdminSummaryResponse(
        Long publicNoticeId,
        String categoryCode,
        String title,
        PublicNoticeStatus status,
        boolean pinned,
        LocalDateTime publishStartDttm,
        LocalDateTime publishEndDttm,
        String updatedByName,
        LocalDateTime updatedDttm
) {
}
