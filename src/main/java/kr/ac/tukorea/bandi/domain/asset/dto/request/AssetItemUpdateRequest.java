package kr.ac.tukorea.bandi.domain.asset.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import kr.ac.tukorea.bandi.domain.asset.model.AssetOwnerType;

public record AssetItemUpdateRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 50) String categoryCode,
        @NotNull AssetOwnerType ownerType,
        @Positive Long ownerMemberId,
        @Size(max = 100) String externalOwnerName,
        @Positive int totalQuantity,
        @NotBlank @Size(max = 200) String storageLocation,
        @Positive Long photoFileId,
        String note
) {

    public AssetItemUpdateParam toParam() {
        return new AssetItemUpdateParam(name, categoryCode, ownerType,
                ownerMemberId, externalOwnerName, totalQuantity,
                storageLocation, photoFileId, note);
    }
}
