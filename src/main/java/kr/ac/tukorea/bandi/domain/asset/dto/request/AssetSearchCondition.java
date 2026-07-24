package kr.ac.tukorea.bandi.domain.asset.dto.request;

import kr.ac.tukorea.bandi.domain.asset.model.AssetStatus;
import kr.ac.tukorea.bandi.domain.asset.model.AssetTrackingType;

public record AssetSearchCondition(
        String keyword,
        String categoryCode,
        AssetTrackingType trackingType,
        AssetStatus status,
        boolean deleted,
        int page,
        int pageSize
) {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    public AssetSearchCondition(String keyword, String categoryCode,
                                AssetTrackingType trackingType,
                                AssetStatus status) {
        this(keyword, categoryCode, trackingType, status, false, 0,
                DEFAULT_PAGE_SIZE);
    }

    public AssetSearchCondition {
        keyword = normalize(keyword);
        categoryCode = normalize(categoryCode);
        page = Math.max(page, 0);
        pageSize = Math.max(1, Math.min(pageSize, MAX_PAGE_SIZE));
    }

    public int offset() {
        return page * pageSize;
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
