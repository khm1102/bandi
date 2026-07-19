package kr.ac.tukorea.bandi.domain.fee.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class FeeChargeNotFoundException extends BusinessException {

    public FeeChargeNotFoundException(Long feeChargeId) {
        super(ErrorCode.FEE_CHARGE_NOT_FOUND, String.valueOf(feeChargeId));
    }
}
