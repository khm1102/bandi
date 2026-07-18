package kr.ac.tukorea.bandi.domain.policy.model;

import kr.ac.tukorea.bandi.domain.policy.exception.InvalidPolicyDocumentException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PolicyDocumentVersionTest {

    private static final LocalDateTime PUBLISHED_AT =
            LocalDateTime.of(2026, 7, 18, 20, 0);

    @Test
    void 정책_버전은_발행_시각과_효력_시작일을_고정한다() {
        PolicyDocumentVersion version = PolicyDocumentVersion.publish(
                1L, 2, "동의 본문", PUBLISHED_AT,
                PUBLISHED_AT.plusDays(1), true, 10L);

        assertThat(version.getVersionNo()).isEqualTo(2);
        assertThat(version.isEffectiveAt(PUBLISHED_AT)).isFalse();
        assertThat(version.isEffectiveAt(PUBLISHED_AT.plusDays(1))).isTrue();
    }

    @Test
    void 효력_시작일은_발행_시각보다_빠를_수_없다() {
        assertThatThrownBy(() -> PolicyDocumentVersion.publish(
                1L, 1, "동의 본문", PUBLISHED_AT,
                PUBLISHED_AT.minusSeconds(1), true, 10L))
                .isInstanceOf(InvalidPolicyDocumentException.class);
    }

    @Test
    void 정책_본문과_버전_번호를_검증한다() {
        assertThatThrownBy(() -> PolicyDocumentVersion.publish(
                1L, 0, "", PUBLISHED_AT, PUBLISHED_AT, true, 10L))
                .isInstanceOf(InvalidPolicyDocumentException.class);
    }
}
