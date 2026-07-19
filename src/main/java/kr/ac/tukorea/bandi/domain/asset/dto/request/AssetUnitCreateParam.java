package kr.ac.tukorea.bandi.domain.asset.dto.request;

public record AssetUnitCreateParam(
        Long assetItemId,
        String managementNo,
        String storageLocation
) {
}
