package kr.ac.tukorea.bandi.domain.asset.model;

import java.time.LocalDateTime;

public record AssetHistory(
        Long assetHistoryId,
        Long assetItemId,
        Long assetUnitId,
        AssetAction action,
        int quantity,
        AssetStatus previousStatus,
        AssetStatus newStatus,
        String note,
        Long changedByMemberId,
        LocalDateTime changedDttm
) {

    public static AssetHistory reserved(AssetUsage usage,
                                        LocalDateTime changedDttm) {
        return new AssetHistory(null, usage.getAssetItemId(),
                usage.getAssetUnitId(), AssetAction.LOAN, usage.getQuantity(),
                AssetStatus.AVAILABLE, AssetStatus.IN_USE, usage.getNote(),
                usage.getCreatedByMemberId(), changedDttm);
    }

    public static AssetHistory returned(AssetUsage usage,
                                        LocalDateTime changedDttm) {
        return new AssetHistory(null, usage.getAssetItemId(),
                usage.getAssetUnitId(), AssetAction.RETURN,
                usage.getQuantity(), AssetStatus.IN_USE,
                AssetStatus.AVAILABLE, usage.getNote(),
                usage.getProcessedByMemberId(), changedDttm);
    }
}
