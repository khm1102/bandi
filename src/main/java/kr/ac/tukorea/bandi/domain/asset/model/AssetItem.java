package kr.ac.tukorea.bandi.domain.asset.model;

import kr.ac.tukorea.bandi.domain.asset.exception.AssetStockUnavailableException;
import kr.ac.tukorea.bandi.domain.asset.exception.InvalidAssetException;
import lombok.Getter;

@Getter
public class AssetItem {

    private Long assetItemId;
    private final String name;
    private final String categoryCode;
    private final AssetTrackingType trackingType;
    private final AssetOwnerType ownerType;
    private final Long ownerMemberId;
    private final String externalOwnerName;
    private final int totalQuantity;
    private final String storageLocation;
    private final AssetStatus status;
    private final Long photoFileId;
    private final String note;

    public AssetItem(Long assetItemId, String name, String categoryCode,
                     AssetTrackingType trackingType, AssetOwnerType ownerType,
                     Long ownerMemberId, String externalOwnerName,
                     int totalQuantity, String storageLocation,
                     AssetStatus status, Long photoFileId, String note) {
        this.assetItemId = assetItemId;
        this.name = name;
        this.categoryCode = categoryCode;
        this.trackingType = trackingType;
        this.ownerType = ownerType;
        this.ownerMemberId = ownerMemberId;
        this.externalOwnerName = externalOwnerName;
        this.totalQuantity = totalQuantity;
        this.storageLocation = storageLocation;
        this.status = status;
        this.photoFileId = photoFileId;
        this.note = note;
    }

    public static AssetItem register(String name, String categoryCode,
                                     AssetTrackingType trackingType,
                                     AssetOwnerType ownerType,
                                     Long ownerMemberId,
                                     String externalOwnerName,
                                     int totalQuantity,
                                     String storageLocation,
                                     Long photoFileId, String note) {
        validate(name, categoryCode, trackingType, ownerType, ownerMemberId,
                externalOwnerName, totalQuantity, storageLocation);
        return new AssetItem(null, name.strip(), categoryCode.strip(),
                trackingType, ownerType, ownerMemberId,
                normalize(externalOwnerName), totalQuantity,
                storageLocation.strip(), AssetStatus.AVAILABLE, photoFileId,
                normalize(note));
    }

    public void validateReservation(Long assetUnitId, int quantity,
                                    int activeQuantity) {
        if (status != AssetStatus.AVAILABLE || quantity < 1) {
            throw new InvalidAssetException();
        }
        if (trackingType == AssetTrackingType.QUANTITY) {
            if (assetUnitId != null || activeQuantity + quantity > totalQuantity) {
                throw new AssetStockUnavailableException();
            }
            return;
        }
        if (assetUnitId == null || quantity != 1) {
            throw new InvalidAssetException();
        }
    }

    private static void validate(String name, String categoryCode,
                                 AssetTrackingType trackingType,
                                 AssetOwnerType ownerType, Long ownerMemberId,
                                 String externalOwnerName, int totalQuantity,
                                 String storageLocation) {
        if (isBlank(name) || isBlank(categoryCode) || trackingType == null
                || ownerType == null || totalQuantity < 1
                || isBlank(storageLocation)) {
            throw new InvalidAssetException();
        }
        boolean validOwner = switch (ownerType) {
            case CLUB -> ownerMemberId == null && isBlank(externalOwnerName);
            case MEMBER -> ownerMemberId != null && isBlank(externalOwnerName);
            case EXTERNAL -> ownerMemberId == null && !isBlank(externalOwnerName);
        };
        if (!validOwner) {
            throw new InvalidAssetException();
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String normalize(String value) {
        return isBlank(value) ? null : value.strip();
    }
}
