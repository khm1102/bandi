package kr.ac.tukorea.bandi.domain.asset.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class AssetStockUnavailableException extends BusinessException {

    public AssetStockUnavailableException() {
        super(ErrorCode.ASSET_STOCK_UNAVAILABLE);
    }
}
