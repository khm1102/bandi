package kr.ac.tukorea.bandi.domain.member.model;

import java.time.LocalDateTime;

/**
 * 동아리 활동 상태 변경 사실 기록. 학교 학적 상태 변경과 구분한다.
 */
public record MemberStatusHistory(
        Long memberStatusHistoryId,
        Long memberId,
        MemberStatus previousStatus,
        MemberStatus newStatus,
        String reason,
        Long changedByMemberId,
        LocalDateTime changedDttm
) {

    public static MemberStatusHistory of(Long memberId, MemberStatus previousStatus, MemberStatus newStatus,
                                         String reason, Long changedByMemberId, LocalDateTime changedDttm) {
        return new MemberStatusHistory(null, memberId, previousStatus, newStatus,
                reason, changedByMemberId, changedDttm);
    }
}
