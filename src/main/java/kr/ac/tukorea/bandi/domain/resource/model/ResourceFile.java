package kr.ac.tukorea.bandi.domain.resource.model;

import lombok.Getter;

@Getter
public class ResourceFile {

    private final Long resourceFileId;
    private final Long resourceId;
    private final Long storedFileId;
    private final int displayOrder;
    private final Long uploadedByMemberId;

    public ResourceFile(Long resourceFileId, Long resourceId, Long storedFileId,
                        int displayOrder, Long uploadedByMemberId) {
        this.resourceFileId = resourceFileId;
        this.resourceId = resourceId;
        this.storedFileId = storedFileId;
        this.displayOrder = displayOrder;
        this.uploadedByMemberId = uploadedByMemberId;
    }

    public static ResourceFile attach(Long resourceId, Long storedFileId,
                                      int displayOrder, Long actorMemberId) {
        return new ResourceFile(null, resourceId, storedFileId, displayOrder, actorMemberId);
    }
}
