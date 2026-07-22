package kr.ac.tukorea.bandi.domain.member.dto.request;

import kr.ac.tukorea.bandi.domain.member.model.ClubRole;

/**
 * 멤버 권한 변경 입력. 처리자는 인증 세션에서 별도로 전달한다.
 */
public record RoleChangeParam(
        Long memberId,
        ClubRole newRole,
        String reason
) {
}
