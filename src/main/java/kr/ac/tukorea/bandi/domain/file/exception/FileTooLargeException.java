package kr.ac.tukorea.bandi.domain.file.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class FileTooLargeException extends BusinessException {

    public FileTooLargeException() {
        super(ErrorCode.FILE_TOO_LARGE);
    }
}
