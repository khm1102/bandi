package kr.ac.tukorea.bandi.domain.asset.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class AssetItemNotFoundException extends BusinessException {

    public AssetItemNotFoundException(Long assetItemId) {
        super(ErrorCode.ASSET_ITEM_NOT_FOUND);
    }
}
