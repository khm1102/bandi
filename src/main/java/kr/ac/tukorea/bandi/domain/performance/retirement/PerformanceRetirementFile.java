package kr.ac.tukorea.bandi.domain.performance.retirement;

import kr.ac.tukorea.bandi.domain.file.model.StorageScope;

public record PerformanceRetirementFile(
        Long storedFileId,
        StorageScope storageScope,
        String storageKey
) {
}
