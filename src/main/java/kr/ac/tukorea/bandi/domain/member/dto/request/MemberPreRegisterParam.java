package kr.ac.tukorea.bandi.domain.member.dto.request;

import kr.ac.tukorea.bandi.domain.member.model.ClubRole;

/**
 * 운영진의 멤버 사전 등록 입력 (정본 F-11 — 학번, 이름, 팀, 기수).
 * registeredByMemberId는 최초 운영진 부트스트랩에서만 null이다(정본 5.5).
 */
public record MemberPreRegisterParam(
        String studentNo,
        String name,
        Long teamId,
        Long cohortId,
        ClubRole role,
        Long registeredByMemberId
) {
}
