package kr.ac.tukorea.bandi.domain.performance.dto.request;

import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPerformanceProjectException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PerformanceProjectSearchConditionTest {

    @Test
    void 빈_학기_코드는_전체_조건으로_정규화한다() {
        PerformanceProjectSearchCondition condition =
                new PerformanceProjectSearchCondition(
                        (short) 2026, " ", null, 0, 20);

        assertThat(condition.termCode()).isNull();
    }

    @Test
    void 페이지_범위와_학년도를_검증한다() {
        assertThatThrownBy(() -> new PerformanceProjectSearchCondition(
                null, null, null, -1, 20))
                .isInstanceOf(InvalidPerformanceProjectException.class);
        assertThatThrownBy(() -> new PerformanceProjectSearchCondition(
                (short) 0, null, null, 0, 20))
                .isInstanceOf(InvalidPerformanceProjectException.class);
        assertThatThrownBy(() -> new PerformanceProjectSearchCondition(
                null, null, null, 0, 101))
                .isInstanceOf(InvalidPerformanceProjectException.class);
    }
}
