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
}
