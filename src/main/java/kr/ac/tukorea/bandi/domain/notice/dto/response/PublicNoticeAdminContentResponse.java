package kr.ac.tukorea.bandi.domain.notice.dto.response;

import kr.ac.tukorea.bandi.domain.notice.model.PublicNoticeStatus;

import java.time.LocalDateTime;

public record PublicNoticeAdminContentResponse(
        Long publicNoticeId,
        String categoryCode,
        String title,
        String body,
        PublicNoticeStatus status,
        boolean pinned,
        LocalDateTime publishStartDttm,
        LocalDateTime publishEndDttm,
        String createdByName,
        String updatedByName,
        LocalDateTime updatedDttm
) {
}
