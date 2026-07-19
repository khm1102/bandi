package kr.ac.tukorea.bandi.domain.notice.dto.request;

import java.util.List;

public record PublicNoticeUpdateParam(
        Long publicNoticeId,
        String categoryCode,
        String title,
        String body,
        boolean pinned,
        List<Long> attachmentFileIds
) {

    public PublicNoticeUpdateParam {
        attachmentFileIds = attachmentFileIds == null
                ? List.of()
                : List.copyOf(attachmentFileIds);
    }
}
