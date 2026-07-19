package kr.ac.tukorea.bandi.domain.asset.dto.request;

import kr.ac.tukorea.bandi.domain.asset.model.AssetOwnerType;

public record AssetItemUpdateParam(
        String name,
        String categoryCode,
        AssetOwnerType ownerType,
        Long ownerMemberId,
        String externalOwnerName,
        int totalQuantity,
        String storageLocation,
        Long photoFileId,
        String note
) {
}
