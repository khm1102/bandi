package kr.ac.tukorea.bandi.domain.asset.dto.request;

import kr.ac.tukorea.bandi.domain.asset.model.AssetOwnerType;
import kr.ac.tukorea.bandi.domain.asset.model.AssetTrackingType;

public record AssetItemCreateParam(
        String name,
        String categoryCode,
        AssetTrackingType trackingType,
        AssetOwnerType ownerType,
        Long ownerMemberId,
        String externalOwnerName,
        int totalQuantity,
        String storageLocation,
        Long photoFileId,
        String note
) {
}
