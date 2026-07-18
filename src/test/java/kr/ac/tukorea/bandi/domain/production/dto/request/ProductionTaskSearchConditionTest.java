package kr.ac.tukorea.bandi.domain.production.dto.request;

import kr.ac.tukorea.bandi.domain.production.exception.InvalidProductionTaskException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductionTaskSearchConditionTest {

    @Test
    void 프로젝트와_페이지_범위가_유효한_검색_조건을_생성한다() {
        ProductionTaskSearchCondition condition =
                new ProductionTaskSearchCondition(
                        1L, null, null, true, 0, 20);

        assertThat(condition.overdueOnly()).isTrue();
    }

    @Test
    void 프로젝트가_없거나_페이지_범위가_잘못되면_거부한다() {
        assertThatThrownBy(() -> new ProductionTaskSearchCondition(
                null, null, null, false, 0, 20))
                .isInstanceOf(InvalidProductionTaskException.class);
        assertThatThrownBy(() -> new ProductionTaskSearchCondition(
                1L, null, null, false, -1, 20))
                .isInstanceOf(InvalidProductionTaskException.class);
        assertThatThrownBy(() -> new ProductionTaskSearchCondition(
                1L, null, null, false, 0, 101))
                .isInstanceOf(InvalidProductionTaskException.class);
    }
}
