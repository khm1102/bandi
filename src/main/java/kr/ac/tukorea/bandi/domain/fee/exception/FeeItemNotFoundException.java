package kr.ac.tukorea.bandi.domain.fee.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class FeeItemNotFoundException extends BusinessException {

    public FeeItemNotFoundException(Long feeItemId) {
        super(ErrorCode.FEE_ITEM_NOT_FOUND, String.valueOf(feeItemId));
    }
}
