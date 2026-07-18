package kr.ac.tukorea.bandi.domain.member.dto.request;

/**
 * 멤버 팀 변경 입력. reason은 정본 5.4의 변경 사유 필수 규칙 때문에 비울 수 없다.
 */
public record TeamChangeParam(
        Long memberId,
        Long newTeamId,
        String reason,
        Long actorMemberId
) {
}
