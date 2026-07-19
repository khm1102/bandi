package kr.ac.tukorea.bandi.domain.asset.dto.response;

import kr.ac.tukorea.bandi.domain.asset.model.AssetStatus;
import kr.ac.tukorea.bandi.domain.asset.model.AssetUnit;

public record AssetUnitResponse(
        Long assetUnitId,
        Long assetItemId,
        String managementNo,
        AssetStatus status,
        String storageLocation
) {

    public static AssetUnitResponse from(AssetUnit unit) {
        return new AssetUnitResponse(unit.getAssetUnitId(),
                unit.getAssetItemId(), unit.getManagementNo(), unit.getStatus(),
                unit.getStorageLocation());
    }
}
