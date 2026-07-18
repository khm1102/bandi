package kr.ac.tukorea.bandi.domain.fee.model;

import kr.ac.tukorea.bandi.domain.fee.exception.InvalidFeeException;
import kr.ac.tukorea.bandi.domain.fee.exception.InvalidFeeStateException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeeChargeTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 9, 1, 12, 0);

    @Test
    void 대상_멤버의_UNPAID_charge를_생성한다() {
        FeeCharge charge = FeeCharge.unpaid(1L, 2L, 30_000L);

        assertThat(charge.getStatus()).isEqualTo(FeeChargeStatus.UNPAID);
        assertThat(charge.getPaidDttm()).isNull();
    }

    @Test
    void PAID_처리는_납부_시각과_처리자를_기록한다() {
        FeeCharge paid = FeeCharge.unpaid(1L, 2L, 30_000L)
                .changeStatus(FeeChargeStatus.PAID, 3L, NOW, "입금 확인");

        assertThat(paid.getPaidDttm()).isEqualTo(NOW);
        assertThat(paid.getProcessedByMemberId()).isEqualTo(3L);
    }

    @Test
    void PAID를_정정하면_납부_시각을_제거한다() {
        FeeCharge paid = FeeCharge.unpaid(1L, 2L, 30_000L)
                .changeStatus(FeeChargeStatus.PAID, 3L, NOW, null);
        FeeCharge unpaid = paid.changeStatus(
                FeeChargeStatus.UNPAID, 3L, NOW.plusMinutes(1), "오입금 정정");

        assertThat(unpaid.getPaidDttm()).isNull();
        assertThat(unpaid.getStatus()).isEqualTo(FeeChargeStatus.UNPAID);
    }

    @Test
    void 같은_상태나_CANCELLED_charge는_변경할_수_없다() {
        FeeCharge unpaid = FeeCharge.unpaid(1L, 2L, 30_000L);
        assertThatThrownBy(() -> unpaid.changeStatus(
                FeeChargeStatus.UNPAID, 3L, NOW, null))
                .isInstanceOf(InvalidFeeException.class);
        FeeCharge cancelled = unpaid.changeStatus(
                FeeChargeStatus.CANCELLED, 3L, NOW, "항목 취소");
        assertThatThrownBy(() -> cancelled.changeStatus(
                FeeChargeStatus.PAID, 3L, NOW, null))
                .isInstanceOf(InvalidFeeStateException.class);
    }
}
