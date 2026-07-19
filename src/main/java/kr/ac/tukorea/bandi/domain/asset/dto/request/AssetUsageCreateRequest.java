package kr.ac.tukorea.bandi.domain.asset.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record AssetUsageCreateRequest(
        @NotNull @Positive Long assetItemId,
        @Positive Long assetUnitId,
        @NotNull @Positive Long performanceProjectId,
        @NotNull @Positive Long teamId,
        @Positive int quantity,
        @NotNull @FutureOrPresent LocalDateTime startDttm,
        @NotNull @FutureOrPresent LocalDateTime expectedReturnDttm,
        @Size(max = 500) String note
) {

    public AssetUsageCreateParam toParam() {
        return new AssetUsageCreateParam(assetItemId, assetUnitId,
                performanceProjectId, teamId, quantity, startDttm,
                expectedReturnDttm, note);
    }
}
