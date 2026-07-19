package kr.ac.tukorea.bandi.domain.production.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class ProductionAccessDeniedException extends BusinessException {

    public ProductionAccessDeniedException() {
        super(ErrorCode.FORBIDDEN);
    }
}
