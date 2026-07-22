package kr.ac.tukorea.bandi.domain.member.exception;

import kr.ac.tukorea.bandi.global.exception.BusinessException;
import kr.ac.tukorea.bandi.global.exception.ErrorCode;

/**
 * 비활성화된 팀에 멤버를 배정하려는 요청.
 * 정본 5.1 — 팀은 기록 보존을 위해 삭제하지 않고 비활성화하며, 기존 참조는 유지된다.
 */
public class InactiveTeamException extends BusinessException {

    public InactiveTeamException(Long teamId) {
        super(ErrorCode.INACTIVE_TEAM, "teamId=" + teamId);
    }
}
