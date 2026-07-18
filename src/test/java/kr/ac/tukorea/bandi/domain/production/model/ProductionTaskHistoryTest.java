package kr.ac.tukorea.bandi.domain.production.model;

import kr.ac.tukorea.bandi.domain.production.exception.InvalidProductionTaskException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductionTaskHistoryTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 3, 2, 12, 0);

    @Test
    void 상태_변경자와_시각과_코멘트를_기록한다() {
        ProductionTaskHistory history = ProductionTaskHistory.change(
                1L, ProductionTaskStatus.TODO,
                ProductionTaskStatus.IN_PROGRESS,
                "제작 시작", 2L, NOW);

        assertThat(history.getChangedByMemberId()).isEqualTo(2L);
        assertThat(history.getComment()).isEqualTo("제작 시작");
    }

    @Test
    void 같은_상태와_긴_코멘트는_이력으로_남기지_않는다() {
        assertThatThrownBy(() -> ProductionTaskHistory.change(
                1L, ProductionTaskStatus.TODO, ProductionTaskStatus.TODO,
                null, 2L, NOW))
                .isInstanceOf(InvalidProductionTaskException.class);
        assertThatThrownBy(() -> ProductionTaskHistory.change(
                1L, ProductionTaskStatus.TODO,
                ProductionTaskStatus.IN_PROGRESS,
                "가".repeat(501), 2L, NOW))
                .isInstanceOf(InvalidProductionTaskException.class);
    }
}
