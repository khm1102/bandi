package kr.ac.tukorea.bandi.domain.file.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class InvalidFileException extends BusinessException {

    public InvalidFileException(String detail) {
        super(ErrorCode.INVALID_FILE, detail);
    }
}
