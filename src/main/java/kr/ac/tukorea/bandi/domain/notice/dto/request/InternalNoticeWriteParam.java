package kr.ac.tukorea.bandi.domain.notice.dto.request;

import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeTargetScope;

import java.util.List;

public record InternalNoticeWriteParam(
        InternalNoticeTargetScope targetScope,
        Long teamId,
        String title,
        String body,
        boolean important,
        List<Long> attachmentFileIds
) {

    public InternalNoticeWriteParam {
        attachmentFileIds = attachmentFileIds == null
                ? List.of()
                : List.copyOf(attachmentFileIds);
    }
}
