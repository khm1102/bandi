package kr.ac.tukorea.bandi.domain.member.model;

import java.time.LocalDateTime;

/**
 * 팀 변경 사실 기록 (정본 5.4). append-only이며 최초 배정은 기록하지 않는다.
 * changedDttm은 Service가 Clock으로 채우는 업무 시각이다.
 */
public record MemberTeamHistory(
        Long memberTeamHistoryId,
        Long memberId,
        Long previousTeamId,
        Long newTeamId,
        String reason,
        Long changedByMemberId,
        LocalDateTime changedDttm
) {

    public static MemberTeamHistory of(Long memberId, Long previousTeamId, Long newTeamId,
                                       String reason, Long changedByMemberId, LocalDateTime changedDttm) {
        return new MemberTeamHistory(null, memberId, previousTeamId, newTeamId,
                reason, changedByMemberId, changedDttm);
    }
}
