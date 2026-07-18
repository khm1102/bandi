package kr.ac.tukorea.bandi.domain.production.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class ProductionTaskNotFoundException extends BusinessException {

    public ProductionTaskNotFoundException(Long productionTaskId) {
        super(ErrorCode.PRODUCTION_TASK_NOT_FOUND,
                String.valueOf(productionTaskId));
    }
}
