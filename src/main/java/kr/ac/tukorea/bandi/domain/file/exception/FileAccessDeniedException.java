package kr.ac.tukorea.bandi.domain.file.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class FileAccessDeniedException extends BusinessException {

    public FileAccessDeniedException() {
        super(ErrorCode.FILE_ACCESS_DENIED);
    }
}
