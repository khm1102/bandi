package kr.ac.tukorea.bandi.domain.file.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class FileStorageUnavailableException extends BusinessException {

    public FileStorageUnavailableException() {
        super(ErrorCode.FILE_STORAGE_UNAVAILABLE);
    }

    public FileStorageUnavailableException(String detail) {
        super(ErrorCode.FILE_STORAGE_UNAVAILABLE, detail);
    }
}
