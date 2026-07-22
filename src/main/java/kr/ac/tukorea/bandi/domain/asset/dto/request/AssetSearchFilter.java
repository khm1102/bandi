package kr.ac.tukorea.bandi.domain.asset.dto.request;

import kr.ac.tukorea.bandi.domain.asset.model.AssetStatus;
import kr.ac.tukorea.bandi.domain.asset.model.AssetTrackingType;

public record AssetSearchFilter(
        AssetTrackingType trackingType,
        AssetStatus status
) {
}
