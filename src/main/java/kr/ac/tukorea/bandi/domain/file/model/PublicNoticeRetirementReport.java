package kr.ac.tukorea.bandi.domain.file.model;

import java.util.List;

public record PublicNoticeRetirementReport(
        int totalCount,
        int pendingCount,
        int deletedCount,
        int retainedSharedCount,
        int failedCount
) {

    public static PublicNoticeRetirementReport from(
            List<PublicNoticeRetirementManifest> manifests) {
        int pendingCount = 0;
        int deletedCount = 0;
        int retainedSharedCount = 0;
        int failedCount = 0;

        for (PublicNoticeRetirementManifest manifest : manifests) {
            switch (manifest.retirementStatus()) {
                case PENDING -> pendingCount++;
                case DELETED -> deletedCount++;
                case RETAINED_SHARED -> retainedSharedCount++;
                case FAILED -> failedCount++;
            }
        }
        return new PublicNoticeRetirementReport(manifests.size(), pendingCount, deletedCount,
                retainedSharedCount, failedCount);
    }
}
