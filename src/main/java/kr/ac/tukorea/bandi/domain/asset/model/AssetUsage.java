package kr.ac.tukorea.bandi.domain.asset.model;

import kr.ac.tukorea.bandi.domain.asset.exception.InvalidAssetUsageStateException;
import kr.ac.tukorea.bandi.domain.asset.exception.InvalidAssetException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AssetUsage {

    private Long assetUsageId;
    private final Long assetItemId;
    private final Long assetUnitId;
    private final Long performanceProjectId;
    private final Long teamId;
    private final int quantity;
    private final AssetUsageStatus status;
    private final LocalDateTime startDttm;
    private final LocalDateTime expectedReturnDttm;
    private final LocalDateTime returnedDttm;
    private final Long createdByMemberId;
    private final Long processedByMemberId;
    private final String note;

    public AssetUsage(Long assetUsageId, Long assetItemId, Long assetUnitId,
                      Long performanceProjectId, Long teamId, int quantity,
                      AssetUsageStatus status, LocalDateTime startDttm,
                      LocalDateTime expectedReturnDttm,
                      LocalDateTime returnedDttm, Long createdByMemberId,
                      Long processedByMemberId, String note) {
        this.assetUsageId = assetUsageId;
        this.assetItemId = assetItemId;
        this.assetUnitId = assetUnitId;
        this.performanceProjectId = performanceProjectId;
        this.teamId = teamId;
        this.quantity = quantity;
        this.status = status;
        this.startDttm = startDttm;
        this.expectedReturnDttm = expectedReturnDttm;
        this.returnedDttm = returnedDttm;
        this.createdByMemberId = createdByMemberId;
        this.processedByMemberId = processedByMemberId;
        this.note = note;
    }

    public static AssetUsage reserve(Long itemId, Long unitId,
                                     Long projectId, Long teamId, int quantity,
                                     LocalDateTime startDttm,
                                     LocalDateTime expectedReturnDttm,
                                     Long actorMemberId, String note) {
        if (itemId == null || projectId == null || teamId == null
                || actorMemberId == null || startDttm == null
                || expectedReturnDttm == null
                || expectedReturnDttm.isBefore(startDttm)) {
            throw new InvalidAssetException();
        }
        return new AssetUsage(null, itemId, unitId, projectId, teamId,
                quantity, AssetUsageStatus.RESERVED, startDttm,
                expectedReturnDttm, null, actorMemberId, actorMemberId, note);
    }

    public AssetUsage returned(Long actorMemberId, LocalDateTime returnedAt) {
        if (status != AssetUsageStatus.RESERVED
                && status != AssetUsageStatus.IN_USE) {
            throw new InvalidAssetUsageStateException();
        }
        return new AssetUsage(assetUsageId, assetItemId, assetUnitId,
                performanceProjectId, teamId, quantity,
                AssetUsageStatus.RETURNED, startDttm, expectedReturnDttm,
                returnedAt, createdByMemberId, actorMemberId, note);
    }
}
