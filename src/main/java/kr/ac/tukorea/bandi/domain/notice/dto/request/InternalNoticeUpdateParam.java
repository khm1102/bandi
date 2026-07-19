package kr.ac.tukorea.bandi.domain.notice.dto.request;

import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeTargetScope;

import java.util.List;

public record InternalNoticeUpdateParam(
        Long internalNoticeId,
        InternalNoticeTargetScope targetScope,
        Long teamId,
        String title,
        String body,
        boolean important,
        List<Long> attachmentFileIds
) {

    public InternalNoticeUpdateParam {
        attachmentFileIds = attachmentFileIds == null
                ? List.of()
                : List.copyOf(attachmentFileIds);
    }
}
