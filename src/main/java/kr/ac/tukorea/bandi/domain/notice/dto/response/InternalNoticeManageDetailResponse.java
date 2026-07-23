package kr.ac.tukorea.bandi.domain.notice.dto.response;

import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeStatus;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeTargetScope;
import kr.ac.tukorea.bandi.domain.notice.service.SafeMarkdownHtml;

import java.time.LocalDateTime;
import java.util.List;

public record InternalNoticeManageDetailResponse(
        Long internalNoticeId,
        InternalNoticeTargetScope targetScope,
        Long teamId,
        String teamName,
        String title,
        String bodyMarkdown,
        SafeMarkdownHtml bodyHtml,
        InternalNoticeStatus status,
        boolean important,
        LocalDateTime publishStartDttm,
        LocalDateTime publishEndDttm,
        String createdByName,
        String updatedByName,
        LocalDateTime updatedDttm,
        List<InternalNoticeAttachmentResponse> attachments
) {

    public static InternalNoticeManageDetailResponse of(
            InternalNoticeManageContentResponse content,
            SafeMarkdownHtml bodyHtml,
            List<InternalNoticeAttachmentResponse> attachments) {
        return new InternalNoticeManageDetailResponse(content.internalNoticeId(),
                content.targetScope(), content.teamId(), content.teamName(), content.title(),
                content.body(), bodyHtml, content.status(), content.important(),
                content.publishStartDttm(), content.publishEndDttm(),
                content.createdByName(), content.updatedByName(), content.updatedDttm(),
                List.copyOf(attachments));
    }
}
