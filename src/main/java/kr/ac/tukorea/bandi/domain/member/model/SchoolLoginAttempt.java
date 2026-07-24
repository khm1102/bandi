package kr.ac.tukorea.bandi.domain.member.model;

import kr.ac.tukorea.bandi.global.config.SchoolLoginAttemptProperties;

import java.time.LocalDateTime;

/**
 * 학교 포털 인증 실패 제한 상태다. 학번 원문 대신 HMAC 해시만 보관한다.
 */
public record SchoolLoginAttempt(
        String studentNoHash,
        Integer failureCount,
        LocalDateTime firstFailureDttm,
        LocalDateTime blockedUntilDttm
) {

    public SchoolLoginAttempt recordFailure(
            LocalDateTime currentDttm,
            SchoolLoginAttemptProperties properties
    ) {
        if (isBlockedAt(currentDttm)) {
            return this;
        }
        if (firstFailureDttm == null || isExpiredAt(currentDttm, properties)) {
            return new SchoolLoginAttempt(studentNoHash, 1, currentDttm, null);
        }
        int nextFailureCount = failureCount + 1;
        LocalDateTime nextBlockedUntilDttm = nextFailureCount >= properties.maxFailures()
                ? currentDttm.plus(properties.cooldown()) : null;
        return new SchoolLoginAttempt(studentNoHash, nextFailureCount, firstFailureDttm,
                nextBlockedUntilDttm);
    }

    public boolean isBlockedAt(LocalDateTime currentDttm) {
        return blockedUntilDttm != null && currentDttm.isBefore(blockedUntilDttm);
    }

    public boolean isExpiredAt(LocalDateTime currentDttm,
                               SchoolLoginAttemptProperties properties) {
        if (isBlockedAt(currentDttm) || firstFailureDttm == null) {
            return false;
        }
        return !currentDttm.isBefore(firstFailureDttm.plus(properties.failureWindow()));
    }
}
