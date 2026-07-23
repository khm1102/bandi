package kr.ac.tukorea.bandi.domain.notice.dto.response;

import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeTargetScope;
import kr.ac.tukorea.bandi.domain.notice.service.SafeMarkdownHtml;

import java.time.LocalDateTime;
import java.util.List;

public record InternalNoticeDetailResponse(
        Long internalNoticeId,
        InternalNoticeTargetScope targetScope,
        Long teamId,
        String teamName,
        String title,
        SafeMarkdownHtml bodyHtml,
        boolean important,
        LocalDateTime publishStartDttm,
        LocalDateTime publishEndDttm,
        String publishedByName,
        LocalDateTime updatedDttm,
        List<InternalNoticeAttachmentResponse> attachments
) {

    public static InternalNoticeDetailResponse of(
            InternalNoticeContentResponse content,
            SafeMarkdownHtml bodyHtml,
            List<InternalNoticeAttachmentResponse> attachments) {
        return new InternalNoticeDetailResponse(content.internalNoticeId(),
                content.targetScope(), content.teamId(), content.teamName(), content.title(),
                bodyHtml, content.important(), content.publishStartDttm(),
                content.publishEndDttm(), content.publishedByName(), content.updatedDttm(),
                List.copyOf(attachments));
    }
}
