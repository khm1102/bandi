package kr.ac.tukorea.bandi.domain.member.dto.request;

import kr.ac.tukorea.bandi.domain.member.model.ClubRole;

/**
 * 멤버 권한 변경 입력. actorMemberId는 본인 강등 차단 판단에 사용한다(정본 5.4).
 */
public record RoleChangeParam(
        Long memberId,
        ClubRole newRole,
        String reason,
        Long actorMemberId
) {
}
