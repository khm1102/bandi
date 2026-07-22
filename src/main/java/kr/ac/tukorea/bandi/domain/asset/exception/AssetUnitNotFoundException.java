package kr.ac.tukorea.bandi.domain.asset.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class AssetUnitNotFoundException extends BusinessException {

    public AssetUnitNotFoundException(Long assetUnitId) {
        super(ErrorCode.ASSET_UNIT_NOT_FOUND);
    }
}
