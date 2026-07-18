package kr.ac.tukorea.bandi.domain.member.dto.request;

import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;

/**
 * 활동 중지·탈퇴·등록 취소 입력. 마지막 활성 운영진은 이 경로로도 제거할 수 없다.
 */
public record StatusChangeParam(
        Long memberId,
        MemberStatus newStatus,
        String reason,
        Long actorMemberId
) {
}
