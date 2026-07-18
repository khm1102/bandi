package kr.ac.tukorea.bandi.domain.performance.model;

import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPerformanceContentException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PerformanceCastHistoryTest {

    private static final LocalDateTime CHANGED_AT =
            LocalDateTime.of(2026, 8, 2, 10, 0);

    @Test
    void 프로젝트_캐스팅_변경_이력을_생성한다() {
        PerformanceCastHistory history = PerformanceCastHistory.project(
                1L, 2L, 3L, 4L, CastType.PRIMARY,
                CastType.ALTERNATE, CastAction.CHANGE,
                "더블 캐스팅 조정", 5L, CHANGED_AT);

        assertThat(history.getScope()).isEqualTo(CastScope.PROJECT);
        assertThat(history.getPerformanceRoundId()).isNull();
    }

    @Test
    void 변경_이력은_이전과_새_프로필이_모두_비어_있을_수_없다() {
        assertThatThrownBy(() -> PerformanceCastHistory.project(
                1L, 2L, null, null, null, null,
                CastAction.CHANGE, null, 5L, CHANGED_AT))
                .isInstanceOf(InvalidPerformanceContentException.class);
    }

    @Test
    void 회차_캐스팅_변경_이력을_생성한다() {
        PerformanceCastHistory history = PerformanceCastHistory.round(
                1L, 2L, 3L, 4L, 5L,
                CastType.PRIMARY, CastType.ALTERNATE,
                CastAction.CHANGE, "회차 출연 변경", 6L, CHANGED_AT);

        assertThat(history.getScope()).isEqualTo(CastScope.ROUND);
        assertThat(history.getPerformanceRoundId()).isEqualTo(2L);
    }
}
