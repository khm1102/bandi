package kr.ac.tukorea.bandi.domain.asset.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AssetUnitCreateRequest(
        @NotBlank @Size(max = 50) String managementNo,
        @NotBlank @Size(max = 200) String storageLocation
) {

    public AssetUnitCreateParam toParam(Long assetItemId) {
        return new AssetUnitCreateParam(assetItemId, managementNo,
                storageLocation);
    }
}
