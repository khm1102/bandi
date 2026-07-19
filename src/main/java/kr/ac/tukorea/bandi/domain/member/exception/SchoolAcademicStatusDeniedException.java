package kr.ac.tukorea.bandi.domain.member.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

public class SchoolAcademicStatusDeniedException extends BusinessException {

    public SchoolAcademicStatusDeniedException() {
        super(ErrorCode.SCHOOL_ACADEMIC_STATUS_DENIED);
    }
}
