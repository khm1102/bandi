package kr.ac.tukorea.bandi.domain.notice.dto.request;

import java.util.List;

public record PublicNoticeWriteParam(
        String categoryCode,
        String title,
        String body,
        boolean pinned,
        List<Long> attachmentFileIds
) {

    public PublicNoticeWriteParam {
        attachmentFileIds = attachmentFileIds == null
                ? List.of()
                : List.copyOf(attachmentFileIds);
    }
}
