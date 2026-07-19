package kr.ac.tukorea.bandi.domain.asset.dto.request;

import kr.ac.tukorea.bandi.domain.asset.model.AssetStatus;
import kr.ac.tukorea.bandi.domain.asset.model.AssetTrackingType;

public record AssetSearchCondition(
        String keyword,
        String categoryCode,
        AssetTrackingType trackingType,
        AssetStatus status
) {

    public AssetSearchCondition {
        keyword = normalize(keyword);
        categoryCode = normalize(categoryCode);
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
