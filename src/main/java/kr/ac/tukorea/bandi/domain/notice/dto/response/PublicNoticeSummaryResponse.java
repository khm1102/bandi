package kr.ac.tukorea.bandi.domain.notice.dto.response;

import java.time.LocalDateTime;

public record PublicNoticeSummaryResponse(
        Long publicNoticeId,
        String categoryCode,
        String title,
        boolean pinned,
        LocalDateTime publishStartDttm,
        String createdByName,
        LocalDateTime updatedDttm
) {
}
