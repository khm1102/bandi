package kr.ac.tukorea.bandi.domain.member.model;

import kr.ac.tukorea.bandi.global.config.SchoolLoginAttemptProperties;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class SchoolLoginAttemptTest {

    private static final LocalDateTime STARTED_AT = LocalDateTime.of(2026, 7, 25, 10, 0);
    private static final SchoolLoginAttemptProperties PROPERTIES =
            new SchoolLoginAttemptProperties(5, Duration.ofMinutes(15),
                    Duration.ofMinutes(15), "test-hmac-secret");

    @Test
    void 다섯번째_실패에서_대기_상태로_전환한다() {
        SchoolLoginAttempt attempt = emptyAttempt();

        for (int failureCount = 1; failureCount <= 5; failureCount++) {
            attempt = attempt.recordFailure(STARTED_AT.plusMinutes(failureCount), PROPERTIES);
        }

        assertThat(attempt.failureCount()).isEqualTo(5);
        assertThat(attempt.isBlockedAt(STARTED_AT.plusMinutes(5))).isTrue();
        assertThat(attempt.blockedUntilDttm()).isEqualTo(STARTED_AT.plusMinutes(20));
    }

    @Test
    void 실패_창이_지나면_기존_실패_횟수를_새로_계산한다() {
        SchoolLoginAttempt attempt = emptyAttempt()
                .recordFailure(STARTED_AT, PROPERTIES)
                .recordFailure(STARTED_AT.plusMinutes(1), PROPERTIES)
                .recordFailure(STARTED_AT.plusMinutes(16), PROPERTIES);

        assertThat(attempt.failureCount()).isOne();
        assertThat(attempt.firstFailureDttm()).isEqualTo(STARTED_AT.plusMinutes(16));
        assertThat(attempt.blockedUntilDttm()).isNull();
    }

    @Test
    void 대기_시간이_지나면_실패_상태를_정리할_수_있다() {
        SchoolLoginAttempt attempt = emptyAttempt();
        for (int failureCount = 1; failureCount <= 5; failureCount++) {
            attempt = attempt.recordFailure(STARTED_AT.plusMinutes(failureCount), PROPERTIES);
        }

        assertThat(attempt.isBlockedAt(STARTED_AT.plusMinutes(20))).isFalse();
        assertThat(attempt.isExpiredAt(STARTED_AT.plusMinutes(20), PROPERTIES)).isTrue();
    }

    private SchoolLoginAttempt emptyAttempt() {
        return new SchoolLoginAttempt("a".repeat(64), 0, null, null);
    }
}
