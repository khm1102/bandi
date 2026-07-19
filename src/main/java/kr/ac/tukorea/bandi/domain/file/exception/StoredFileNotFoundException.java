package kr.ac.tukorea.bandi.domain.file.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class StoredFileNotFoundException extends BusinessException {

    public StoredFileNotFoundException(Long storedFileId) {
        super(ErrorCode.FILE_NOT_FOUND, String.valueOf(storedFileId));
    }
}
