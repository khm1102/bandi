package kr.ac.tukorea.bandi.domain.asset.dto.response;

import kr.ac.tukorea.bandi.domain.asset.model.AssetItem;
import kr.ac.tukorea.bandi.domain.asset.model.AssetOwnerType;
import kr.ac.tukorea.bandi.domain.asset.model.AssetStatus;
import kr.ac.tukorea.bandi.domain.asset.model.AssetTrackingType;

public record AssetItemResponse(
        Long assetItemId,
        String name,
        String categoryCode,
        AssetTrackingType trackingType,
        AssetOwnerType ownerType,
        Long ownerMemberId,
        String externalOwnerName,
        int totalQuantity,
        String storageLocation,
        AssetStatus status,
        Long photoFileId,
        String note
) {

    public static AssetItemResponse from(AssetItem item) {
        return new AssetItemResponse(item.getAssetItemId(), item.getName(),
                item.getCategoryCode(), item.getTrackingType(),
                item.getOwnerType(), item.getOwnerMemberId(),
                item.getExternalOwnerName(), item.getTotalQuantity(),
                item.getStorageLocation(), item.getStatus(),
                item.getPhotoFileId(), item.getNote());
    }
}
