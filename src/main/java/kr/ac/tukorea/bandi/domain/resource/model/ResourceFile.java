package kr.ac.tukorea.bandi.domain.resource.model;

import kr.ac.tukorea.bandi.domain.resource.exception.InvalidResourceException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ResourceFile {

    private Long resourceFileId;
    private final Long resourceId;
    private final Long storedFileId;
    private final int revisionNo;
    private final int displayOrder;
    private final Long uploadedByMemberId;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;

    public ResourceFile(Long resourceFileId, Long resourceId, Long storedFileId,
                        int revisionNo, int displayOrder, Long uploadedByMemberId,
                        LocalDateTime createdDttm, LocalDateTime updatedDttm) {
        if (resourceId == null || storedFileId == null || revisionNo < 1
                || displayOrder < 0 || uploadedByMemberId == null) {
            throw new InvalidResourceException("file");
        }
        this.resourceFileId = resourceFileId;
        this.resourceId = resourceId;
        this.storedFileId = storedFileId;
        this.revisionNo = revisionNo;
        this.displayOrder = displayOrder;
        this.uploadedByMemberId = uploadedByMemberId;
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
    }

    public static ResourceFile create(Long resourceId, Long storedFileId,
                                      int revisionNo, int displayOrder,
                                      Long uploadedByMemberId) {
        return new ResourceFile(null, resourceId, storedFileId, revisionNo,
                displayOrder, uploadedByMemberId, null, null);
    }
}
