package kr.ac.tukorea.bandi.domain.file.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * MyBatis가 INSERT 생성 키를 주입해야 하므로 manifestId에만 setter를 둔다
 * (컨벤션 6.6의 persistence 객체 예외).
 */
@Getter
public class ProfilePhotoRetirementManifest {

    @Setter
    private Long manifestId;
    private final Long storedFileId;
    private final StorageScope storageScope;
    private final String storageKey;
    private final ProfilePhotoRetirementStatus status;
    private final Integer attemptCount;
    private final String failureReason;
    private final LocalDateTime completedDttm;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;

    public ProfilePhotoRetirementManifest(Long manifestId, Long storedFileId,
                                          StorageScope storageScope, String storageKey,
                                          ProfilePhotoRetirementStatus status, Integer attemptCount,
                                          String failureReason, LocalDateTime completedDttm,
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

    public static ProfilePhotoRetirementManifest pending(StoredFile file) {
        file.validateProfileImage();
        return new ProfilePhotoRetirementManifest(null, file.getStoredFileId(),
                file.getStorageScope(), file.getStorageKey(),
                ProfilePhotoRetirementStatus.PENDING, 0, null, null,
                null, null);
    }
}
