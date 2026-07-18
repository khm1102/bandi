package kr.ac.tukorea.bandi.domain.file.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class InvalidFileStateException extends BusinessException {

    public InvalidFileStateException() {
        super(ErrorCode.INVALID_FILE_STATE);
    }
}
