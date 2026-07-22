package kr.ac.tukorea.bandi.domain.file.model;

import java.time.LocalDateTime;
import java.util.Objects;
import kr.ac.tukorea.bandi.domain.file.exception.InvalidFileScopeException;
import kr.ac.tukorea.bandi.domain.file.exception.InvalidFileStateException;
import lombok.Getter;

@Getter
public class StoredFile {

    private Long storedFileId;
    private final String originalName;
    private final StorageScope storageScope;
    private final String storageKey;
    private final String contentType;
    private final long sizeBytes;
    private final String sha256Hash;
    private String objectEtag;
    private final Long uploadedByMemberId;
    private UploadStatus uploadStatus;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;
    private final LocalDateTime deletedDttm;

    public StoredFile(Long storedFileId, String originalName, StorageScope storageScope,
                      String storageKey, String contentType, Long sizeBytes, String sha256Hash,
                      String objectEtag, Long uploadedByMemberId, UploadStatus uploadStatus,
                      LocalDateTime createdDttm, LocalDateTime updatedDttm,
                      LocalDateTime deletedDttm) {
        this.storedFileId = storedFileId;
        this.originalName = originalName;
        this.storageScope = storageScope;
        this.storageKey = storageKey;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.sha256Hash = sha256Hash;
        this.objectEtag = objectEtag;
        this.uploadedByMemberId = uploadedByMemberId;
        this.uploadStatus = uploadStatus;
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
        this.deletedDttm = deletedDttm;
    }

    public static StoredFile pending(String originalName, StorageScope storageScope,
                                     String storageKey, String contentType, long sizeBytes,
                                     String sha256Hash, Long uploadedByMemberId) {
        return new StoredFile(null, originalName, storageScope, storageKey, contentType,
                sizeBytes, sha256Hash, null, uploadedByMemberId, UploadStatus.PENDING,
                null, null, null);
    }

    public void markReady(String objectEtag) {
        validatePending();
        if (objectEtag == null || objectEtag.isBlank()) {
            throw new InvalidFileStateException();
        }
        this.objectEtag = objectEtag;
        this.uploadStatus = UploadStatus.READY;
    }

    public void markFailed() {
        validatePending();
        uploadStatus = UploadStatus.FAILED;
    }

    public void validateReady() {
        if (uploadStatus != UploadStatus.READY) {
            throw new InvalidFileStateException();
        }
    }

    public void validatePrivateDownload() {
        validateReady();
        if (storageScope != StorageScope.PRIVATE) {
            throw new InvalidFileScopeException();
        }
    }

    public void validatePublicUse() {
        validateReady();
        if (storageScope != StorageScope.PUBLIC) {
            throw new InvalidFileScopeException();
        }
    }

    public boolean isUploadedBy(Long memberId) {
        return Objects.equals(uploadedByMemberId, memberId);
    }

    public StoredFile createPublicPromotion(String publicStorageKey, Long uploadedByMemberId) {
        validateReady();
        if (storageScope != StorageScope.PRIVATE) {
            throw new InvalidFileScopeException();
        }
        return pending(originalName, StorageScope.PUBLIC, publicStorageKey, contentType,
                sizeBytes, sha256Hash, uploadedByMemberId);
    }

    private void validatePending() {
        if (uploadStatus != UploadStatus.PENDING) {
            throw new InvalidFileStateException();
        }
    }
}
