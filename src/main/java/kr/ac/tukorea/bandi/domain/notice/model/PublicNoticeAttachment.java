package kr.ac.tukorea.bandi.domain.notice.model;

import kr.ac.tukorea.bandi.domain.notice.exception.InvalidPublicNoticeException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PublicNoticeAttachment {

    private Long publicNoticeAttachmentId;
    private final Long publicNoticeId;
    private final Long storedFileId;
    private final int displayOrder;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;

    public PublicNoticeAttachment(Long publicNoticeAttachmentId, Long publicNoticeId,
                                  Long storedFileId, int displayOrder,
                                  LocalDateTime createdDttm, LocalDateTime updatedDttm) {
        validate(publicNoticeId, storedFileId, displayOrder);
        this.publicNoticeAttachmentId = publicNoticeAttachmentId;
        this.publicNoticeId = publicNoticeId;
        this.storedFileId = storedFileId;
        this.displayOrder = displayOrder;
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
    }

    public static PublicNoticeAttachment create(Long publicNoticeId, Long storedFileId,
                                                int displayOrder) {
        return new PublicNoticeAttachment(null, publicNoticeId, storedFileId, displayOrder,
                null, null);
    }

    private void validate(Long noticeId, Long fileId, int order) {
        if (noticeId == null || fileId == null || order < 0) {
            throw new InvalidPublicNoticeException("attachment");
        }
    }
}
