package kr.ac.tukorea.bandi.domain.member.model;

import java.time.LocalDateTime;

/**
 * 권한 변경 사실 기록 (정본 5.4). 감사와 분쟁 확인에 사용한다.
 */
public record MemberRoleHistory(
        Long memberRoleHistoryId,
        Long memberId,
        ClubRole previousRole,
        ClubRole newRole,
        String reason,
        Long changedByMemberId,
        LocalDateTime changedDttm
) {

    public static MemberRoleHistory of(Long memberId, ClubRole previousRole, ClubRole newRole,
                                       String reason, Long changedByMemberId, LocalDateTime changedDttm) {
        return new MemberRoleHistory(null, memberId, previousRole, newRole,
                reason, changedByMemberId, changedDttm);
    }
}
