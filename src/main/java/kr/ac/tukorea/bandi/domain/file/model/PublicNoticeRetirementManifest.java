package kr.ac.tukorea.bandi.domain.file.model;

import java.time.LocalDateTime;

public record PublicNoticeRetirementManifest(
        Long publicNoticeRetirementManifestId,
        Long storedFileId,
        StorageScope storageScope,
        String storageKey,
        PublicNoticeRetirementStatus retirementStatus,
        String failureReason,
        LocalDateTime createdDttm,
        LocalDateTime processedDttm
) {
}
