package kr.ac.tukorea.bandi.domain.asset.dto.request;

import java.time.LocalDateTime;

public record AssetUsageCreateParam(
        Long assetItemId,
        Long assetUnitId,
        Long performanceProjectId,
        Long teamId,
        int quantity,
        LocalDateTime startDttm,
        LocalDateTime expectedReturnDttm,
        String note
) {
}
