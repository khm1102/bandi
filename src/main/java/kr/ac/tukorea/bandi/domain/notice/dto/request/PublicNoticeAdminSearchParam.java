package kr.ac.tukorea.bandi.domain.notice.dto.request;

import kr.ac.tukorea.bandi.domain.notice.model.PublicNoticeStatus;

public record PublicNoticeAdminSearchParam(
        String keyword,
        PublicNoticeStatus status,
        int page,
        int pageSize
) {

    public PublicNoticeAdminSearchParam {
        new PublicNoticeSearchParam(keyword, page, pageSize);
    }
}
