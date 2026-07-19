package kr.ac.tukorea.bandi.domain.asset.model;

import kr.ac.tukorea.bandi.domain.asset.exception.AssetStockUnavailableException;
import kr.ac.tukorea.bandi.domain.asset.exception.InvalidAssetException;
import lombok.Getter;

import java.util.Objects;

@Getter
public class AssetUnit {

    private Long assetUnitId;
    private final Long assetItemId;
    private final String managementNo;
    private final AssetStatus status;
    private final String storageLocation;

    public AssetUnit(Long assetUnitId, Long assetItemId, String managementNo,
                     AssetStatus status, String storageLocation) {
        this.assetUnitId = assetUnitId;
        this.assetItemId = assetItemId;
        this.managementNo = managementNo;
        this.status = status;
        this.storageLocation = storageLocation;
    }

    public static AssetUnit register(Long assetItemId, String managementNo,
                                     String storageLocation) {
        if (assetItemId == null || managementNo == null
                || managementNo.isBlank() || storageLocation == null
                || storageLocation.isBlank()) {
            throw new InvalidAssetException();
        }
        return new AssetUnit(null, assetItemId, managementNo.strip(),
                AssetStatus.AVAILABLE, storageLocation.strip());
    }

    public void validateReservation(Long expectedItemId,
                                    boolean alreadyInUse) {
        if (!Objects.equals(assetItemId, expectedItemId)
                || status != AssetStatus.AVAILABLE || alreadyInUse) {
            throw new AssetStockUnavailableException();
        }
    }

    public AssetUnit changeStatus(AssetStatus newStatus) {
        if (newStatus == null) {
            throw new InvalidAssetException();
        }
        return new AssetUnit(assetUnitId, assetItemId, managementNo, newStatus,
                storageLocation);
    }

    public AssetUnit edit(AssetStatus newStatus, String newStorageLocation) {
        if (newStatus == null || newStorageLocation == null
                || newStorageLocation.isBlank()) {
            throw new InvalidAssetException();
        }
        return new AssetUnit(assetUnitId, assetItemId, managementNo, newStatus,
                newStorageLocation.strip());
    }
}
