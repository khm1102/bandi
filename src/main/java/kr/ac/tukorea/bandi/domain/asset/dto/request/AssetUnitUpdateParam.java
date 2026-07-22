package kr.ac.tukorea.bandi.domain.asset.dto.request;

import kr.ac.tukorea.bandi.domain.asset.model.AssetStatus;

public record AssetUnitUpdateParam(
        Long assetUnitId,
        AssetStatus status,
        String storageLocation,
        String note
) {
}
