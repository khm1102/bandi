package kr.ac.tukorea.bandi.domain.member.dto.request;

/**
 * 멤버 기수 변경 입력. 처리자는 인증 세션에서 별도로 전달한다.
 */
public record CohortChangeParam(
        Long memberId,
        Long newCohortId,
        String reason
) {
}
