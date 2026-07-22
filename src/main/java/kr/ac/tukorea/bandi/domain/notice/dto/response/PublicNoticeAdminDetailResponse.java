package kr.ac.tukorea.bandi.domain.notice.dto.response;

import kr.ac.tukorea.bandi.domain.notice.model.PublicNoticeStatus;

import java.time.LocalDateTime;
import java.util.List;

public record PublicNoticeAdminDetailResponse(
        Long publicNoticeId,
        String categoryCode,
        String title,
        String body,
        PublicNoticeStatus status,
        boolean pinned,
        LocalDateTime publishStartDttm,
        LocalDateTime publishEndDttm,
        String createdByName,
        String updatedByName,
        LocalDateTime updatedDttm,
        List<PublicNoticeAttachmentResponse> attachments
) {

    public static PublicNoticeAdminDetailResponse of(
            PublicNoticeAdminContentResponse content,
            List<PublicNoticeAttachmentResponse> attachments) {
        return new PublicNoticeAdminDetailResponse(content.publicNoticeId(),
                content.categoryCode(), content.title(), content.body(), content.status(),
                content.pinned(), content.publishStartDttm(), content.publishEndDttm(),
                content.createdByName(), content.updatedByName(), content.updatedDttm(),
                List.copyOf(attachments));
    }
}
