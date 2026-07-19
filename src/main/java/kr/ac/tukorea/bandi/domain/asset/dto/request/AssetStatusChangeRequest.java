package kr.ac.tukorea.bandi.domain.asset.dto.request;

import jakarta.validation.constraints.NotNull;
import kr.ac.tukorea.bandi.domain.asset.model.AssetStatus;

public record AssetStatusChangeRequest(
        @NotNull AssetStatus status,
        String note
) {
}
