package kr.ac.tukorea.bandi.domain.notice.dto.response;

import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeTargetScope;

import java.time.LocalDateTime;
import java.util.List;

public record InternalNoticeDetailResponse(
        Long internalNoticeId,
        InternalNoticeTargetScope targetScope,
        Long teamId,
        String teamName,
        String title,
        String body,
        boolean important,
        LocalDateTime publishStartDttm,
        LocalDateTime publishEndDttm,
        String publishedByName,
        LocalDateTime updatedDttm,
        List<InternalNoticeAttachmentResponse> attachments
) {

    public static InternalNoticeDetailResponse of(
            InternalNoticeContentResponse content,
            List<InternalNoticeAttachmentResponse> attachments) {
        return new InternalNoticeDetailResponse(content.internalNoticeId(),
                content.targetScope(), content.teamId(), content.teamName(), content.title(),
                content.body(), content.important(), content.publishStartDttm(),
                content.publishEndDttm(), content.publishedByName(), content.updatedDttm(),
                List.copyOf(attachments));
    }
}
