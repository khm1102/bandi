package kr.ac.tukorea.bandi.domain.notice.model;

import kr.ac.tukorea.bandi.domain.notice.exception.InvalidInternalNoticeException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class InternalNoticeAttachment {

    private Long internalNoticeAttachmentId;
    private final Long internalNoticeId;
    private final Long storedFileId;
    private final int displayOrder;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;

    public InternalNoticeAttachment(Long internalNoticeAttachmentId,
                                    Long internalNoticeId, Long storedFileId,
                                    int displayOrder, LocalDateTime createdDttm,
                                    LocalDateTime updatedDttm) {
        if (internalNoticeId == null || storedFileId == null || displayOrder < 0) {
            throw new InvalidInternalNoticeException("attachment");
        }
        this.internalNoticeAttachmentId = internalNoticeAttachmentId;
        this.internalNoticeId = internalNoticeId;
        this.storedFileId = storedFileId;
        this.displayOrder = displayOrder;
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
    }

    public static InternalNoticeAttachment create(Long internalNoticeId,
                                                  Long storedFileId,
                                                  int displayOrder) {
        return new InternalNoticeAttachment(null, internalNoticeId, storedFileId,
                displayOrder, null, null);
    }
}
