package kr.ac.tukorea.bandi.domain.notice.dto.request;

import java.time.LocalDateTime;

public record NoticePublishRequest(
        LocalDateTime publishStartDttm,
        LocalDateTime publishEndDttm
) {

    public PublicNoticePublishParam toPublicParam(Long publicNoticeId) {
        return new PublicNoticePublishParam(publicNoticeId, publishStartDttm,
                publishEndDttm);
    }

    public InternalNoticePublishParam toInternalParam(Long internalNoticeId) {
        return new InternalNoticePublishParam(internalNoticeId, publishStartDttm,
                publishEndDttm);
    }
}
