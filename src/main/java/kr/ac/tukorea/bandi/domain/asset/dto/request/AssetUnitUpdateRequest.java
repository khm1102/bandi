package kr.ac.tukorea.bandi.domain.asset.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kr.ac.tukorea.bandi.domain.asset.model.AssetStatus;

public record AssetUnitUpdateRequest(
        @NotNull AssetStatus status,
        @NotBlank @Size(max = 200) String storageLocation,
        String note
) {

    public AssetUnitUpdateParam toParam(Long assetUnitId) {
        return new AssetUnitUpdateParam(assetUnitId, status,
                storageLocation, note);
    }
}
