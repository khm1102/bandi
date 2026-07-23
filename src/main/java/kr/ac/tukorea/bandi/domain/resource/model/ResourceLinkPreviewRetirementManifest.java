package kr.ac.tukorea.bandi.domain.resource.model;

import kr.ac.tukorea.bandi.domain.file.model.StorageScope;
import kr.ac.tukorea.bandi.domain.file.model.StoredFile;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
public class ResourceLinkPreviewRetirementManifest {

    @Setter
    private Long manifestId;
    private final Long storedFileId;
    private final StorageScope storageScope;
    private final String storageKey;
    private final ResourceLinkPreviewRetirementStatus status;
    private final Integer attemptCount;
    private final String failureReason;
    private final LocalDateTime completedDttm;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;

    public ResourceLinkPreviewRetirementManifest(Long manifestId, Long storedFileId,
                                                  StorageScope storageScope, String storageKey,
                                                  ResourceLinkPreviewRetirementStatus status,
                                                  Integer attemptCount, String failureReason,
                                                  LocalDateTime completedDttm,
                                                  LocalDateTime createdDttm,
                                                  LocalDateTime updatedDttm) {
        this.manifestId = manifestId;
        this.storedFileId = storedFileId;
        this.storageScope = storageScope;
        this.storageKey = storageKey;
        this.status = status;
        this.attemptCount = attemptCount;
        this.failureReason = failureReason;
        this.completedDttm = completedDttm;
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
    }

    public static ResourceLinkPreviewRetirementManifest pending(StoredFile file) {
        file.validatePrivateDownload();
        return new ResourceLinkPreviewRetirementManifest(null, file.getStoredFileId(),
                file.getStorageScope(), file.getStorageKey(),
                ResourceLinkPreviewRetirementStatus.PENDING, 0, null, null,
                null, null);
    }
}
