package kr.ac.tukorea.bandi.domain.asset.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class AssetAccessDeniedException extends BusinessException {

    public AssetAccessDeniedException() {
        super(ErrorCode.ASSET_ACCESS_DENIED);
    }
}
