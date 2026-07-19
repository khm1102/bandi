package kr.ac.tukorea.bandi.domain.asset.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class AssetUsageNotFoundException extends BusinessException {

    public AssetUsageNotFoundException(Long assetUsageId) {
        super(ErrorCode.ASSET_USAGE_NOT_FOUND);
    }
}
