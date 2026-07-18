package kr.ac.tukorea.bandi.domain.fee.model;

import kr.ac.tukorea.bandi.domain.fee.exception.InvalidFeeException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeeChargeHistoryTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 1, 12, 0);

    @Test
    void 수납_상태와_금액과_변경자를_기록한다() {
        FeeChargeHistory history = FeeChargeHistory.change(
                1L, FeeChargeStatus.UNPAID, FeeChargeStatus.PAID,
                30_000L, "입금", 2L, NOW);

        assertThat(history.getAmount()).isEqualTo(30_000L);
        assertThat(history.getChangedByMemberId()).isEqualTo(2L);
    }

    @Test
    void 같은_상태나_500자를_넘는_사유는_이력으로_남기지_않는다() {
        assertThatThrownBy(() -> FeeChargeHistory.change(
                1L, FeeChargeStatus.PAID, FeeChargeStatus.PAID,
                30_000L, null, 2L, NOW))
                .isInstanceOf(InvalidFeeException.class);
        assertThatThrownBy(() -> FeeChargeHistory.change(
                1L, FeeChargeStatus.UNPAID, FeeChargeStatus.PAID,
                30_000L, "가".repeat(501), 2L, NOW))
                .isInstanceOf(InvalidFeeException.class);
    }
}
