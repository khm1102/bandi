package kr.ac.tukorea.bandi.domain.fee.model;

import kr.ac.tukorea.bandi.domain.fee.exception.InvalidFeeException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FeeAmountTest {

    @Test
    void 양수인_회비_금액을_생성한다() {
        FeeAmount amount = new FeeAmount(30_000L);

        assertThat(amount.value()).isEqualTo(30_000L);
    }

    @Test
    void 영원_이하의_회비_금액은_생성할_수_없다() {
        assertThatThrownBy(() -> new FeeAmount(0))
                .isInstanceOf(InvalidFeeException.class);
        assertThatThrownBy(() -> new FeeAmount(-1))
                .isInstanceOf(InvalidFeeException.class);
    }
}
