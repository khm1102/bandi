package kr.ac.tukorea.bandi.domain.notice.dto.response;

import java.time.LocalDateTime;

public record PublicNoticeContentResponse(
        Long publicNoticeId,
        String categoryCode,
        String title,
        String body,
        boolean pinned,
        LocalDateTime publishStartDttm,
        LocalDateTime publishEndDttm,
        String createdByName,
        String updatedByName,
        LocalDateTime updatedDttm
) {
}
