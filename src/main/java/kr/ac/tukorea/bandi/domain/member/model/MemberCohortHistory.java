package kr.ac.tukorea.bandi.domain.member.model;

import java.time.LocalDateTime;

/**
 * 기수 변경 사실 기록. append-only이며 최초 배정은 기록하지 않는다.
 */
public record MemberCohortHistory(
        Long memberCohortHistoryId,
        Long memberId,
        Long previousCohortId,
        Long newCohortId,
        String reason,
        Long changedByMemberId,
        LocalDateTime changedDttm
) {

    public static MemberCohortHistory of(Long memberId, Long previousCohortId, Long newCohortId,
                                         String reason, Long changedByMemberId, LocalDateTime changedDttm) {
        return new MemberCohortHistory(null, memberId, previousCohortId, newCohortId,
                reason, changedByMemberId, changedDttm);
    }
}
