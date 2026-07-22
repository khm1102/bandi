package kr.ac.tukorea.bandi.domain.asset.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class InvalidAssetException extends BusinessException {

    public InvalidAssetException() {
        super(ErrorCode.INVALID_ASSET);
    }
}
