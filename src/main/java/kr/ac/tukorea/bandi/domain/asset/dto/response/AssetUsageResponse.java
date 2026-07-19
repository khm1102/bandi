package kr.ac.tukorea.bandi.domain.asset.dto.response;

import kr.ac.tukorea.bandi.domain.asset.model.AssetUsage;
import kr.ac.tukorea.bandi.domain.asset.model.AssetUsageStatus;

import java.time.LocalDateTime;

public record AssetUsageResponse(
        Long assetUsageId,
        Long assetItemId,
        Long assetUnitId,
        Long performanceProjectId,
        Long teamId,
        int quantity,
        AssetUsageStatus status,
        LocalDateTime startDttm,
        LocalDateTime expectedReturnDttm,
        LocalDateTime returnedDttm,
        Long createdByMemberId,
        Long processedByMemberId,
        String note
) {

    public static AssetUsageResponse from(AssetUsage usage) {
        return new AssetUsageResponse(usage.getAssetUsageId(),
                usage.getAssetItemId(), usage.getAssetUnitId(),
                usage.getPerformanceProjectId(), usage.getTeamId(),
                usage.getQuantity(), usage.getStatus(), usage.getStartDttm(),
                usage.getExpectedReturnDttm(), usage.getReturnedDttm(),
                usage.getCreatedByMemberId(), usage.getProcessedByMemberId(),
                usage.getNote());
    }
}
