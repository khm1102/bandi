package kr.ac.tukorea.bandi.domain.member.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

/**
 * 학번은 서비스 전체에서 고유하다 (정본 5.3, uk_member_student_no).
 * 등록 취소된 멤버도 학번을 계속 점유하므로 정정은 기존 행을 수정해서 처리한다.
 */
public class DuplicateStudentNoException extends BusinessException {

    public DuplicateStudentNoException() {
        super(ErrorCode.DUPLICATE_STUDENT_NO);
    }
}
