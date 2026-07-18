package kr.ac.tukorea.bandi.domain.notice.dto.request;

import java.time.LocalDateTime;

public record PublicNoticePublishParam(
        Long publicNoticeId,
        LocalDateTime publishStartDttm,
        LocalDateTime publishEndDttm
) {
}
