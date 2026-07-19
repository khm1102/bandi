package kr.ac.tukorea.bandi.domain.asset.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class InvalidAssetUsageStateException extends BusinessException {

    public InvalidAssetUsageStateException() {
        super(ErrorCode.INVALID_ASSET_USAGE_STATE);
    }
}
