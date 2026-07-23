package kr.ac.tukorea.bandi.domain.notice.dto.response;

import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeTargetScope;

import java.time.LocalDateTime;

public record InternalNoticeSummaryResponse(
        Long internalNoticeId,
        InternalNoticeTargetScope targetScope,
        Long teamId,
        String teamName,
        String title,
        String createdByName,
        boolean important,
        LocalDateTime publishStartDttm,
        LocalDateTime publishEndDttm,
        boolean read
) {
}
