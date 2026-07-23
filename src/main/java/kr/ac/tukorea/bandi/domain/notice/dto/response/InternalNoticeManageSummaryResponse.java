package kr.ac.tukorea.bandi.domain.notice.dto.response;

import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeStatus;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeTargetScope;

import java.time.LocalDateTime;

public record InternalNoticeManageSummaryResponse(
        Long internalNoticeId,
        InternalNoticeTargetScope targetScope,
        Long teamId,
        String teamName,
        String title,
        InternalNoticeStatus status,
        boolean important,
        LocalDateTime publishStartDttm,
        LocalDateTime publishEndDttm,
        String createdByName,
        String updatedByName,
        LocalDateTime updatedDttm
) {
}
