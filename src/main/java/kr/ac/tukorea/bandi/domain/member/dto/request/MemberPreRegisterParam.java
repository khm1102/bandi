package kr.ac.tukorea.bandi.domain.member.dto.request;

import kr.ac.tukorea.bandi.domain.member.model.ClubRole;

/**
 * 운영진의 멤버 사전 등록 입력 (정본 F-11 — 학번, 이름, 팀, 기수).
 * 등록 처리자는 인증 세션에서 별도로 전달한다. 최초 운영진은 정본 5.5에 따라
 * 애플리케이션 Service가 아닌 운영 DB 관리 채널에서 부트스트랩한다.
 */
public record MemberPreRegisterParam(
        String studentNo,
        String name,
        Long teamId,
        Long cohortId,
        ClubRole role
) {
}
