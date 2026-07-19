package kr.ac.tukorea.bandi.domain.notice.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record PublicNoticeDetailResponse(
        Long publicNoticeId,
        String categoryCode,
        String title,
        String body,
        boolean pinned,
        LocalDateTime publishStartDttm,
        LocalDateTime publishEndDttm,
        String createdByName,
        String updatedByName,
        LocalDateTime updatedDttm,
        List<PublicNoticeAttachmentResponse> attachments
) {

    public static PublicNoticeDetailResponse of(
            PublicNoticeContentResponse content,
            List<PublicNoticeAttachmentResponse> attachments) {
        return new PublicNoticeDetailResponse(content.publicNoticeId(),
                content.categoryCode(), content.title(), content.body(), content.pinned(),
                content.publishStartDttm(), content.publishEndDttm(),
                content.createdByName(), content.updatedByName(), content.updatedDttm(),
                List.copyOf(attachments));
    }
}
