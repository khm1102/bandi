package kr.ac.tukorea.bandi.domain.asset.dto.response;

import kr.ac.tukorea.bandi.domain.asset.model.AssetAction;
import kr.ac.tukorea.bandi.domain.asset.model.AssetHistory;
import kr.ac.tukorea.bandi.domain.asset.model.AssetStatus;

import java.time.LocalDateTime;

public record AssetHistoryResponse(
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

    public static AssetHistoryResponse from(AssetHistory history) {
        return new AssetHistoryResponse(history.assetHistoryId(),
                history.assetItemId(), history.assetUnitId(),
                history.action(), history.quantity(),
                history.previousStatus(), history.newStatus(), history.note(),
                history.changedByMemberId(), history.changedDttm());
    }
}
