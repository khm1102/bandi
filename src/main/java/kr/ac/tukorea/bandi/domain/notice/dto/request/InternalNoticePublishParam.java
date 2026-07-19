package kr.ac.tukorea.bandi.domain.notice.dto.request;

import java.time.LocalDateTime;

public record InternalNoticePublishParam(
        Long internalNoticeId,
        LocalDateTime publishStartDttm,
        LocalDateTime publishEndDttm
) {
}
