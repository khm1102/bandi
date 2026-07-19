package kr.ac.tukorea.bandi.domain.performance.model;

import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPerformanceContentException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PerformanceCastTest {

    @Test
    void 프로젝트_배역과_공개_프로필을_캐스팅한다() {
        PerformanceCast cast = PerformanceCast.assign(
                1L, 2L, 3L, CastType.PRIMARY, 0);

        assertThat(cast.getCastType()).isEqualTo(CastType.PRIMARY);
        assertThat(cast.getPublicProfileId()).isEqualTo(3L);
    }

    @Test
    void 캐스팅을_다른_공개_프로필과_타입으로_변경한다() {
        PerformanceCast changed = PerformanceCast.assign(
                        1L, 2L, 3L, CastType.ALTERNATE, 1)
                .change(4L, CastType.UNDERSTUDY, 2);

        assertThat(changed.getPublicProfileId()).isEqualTo(4L);
        assertThat(changed.getCastType()).isEqualTo(CastType.UNDERSTUDY);
    }

    @Test
    void 같은_값으로_캐스팅을_변경할_수_없다() {
        PerformanceCast cast = PerformanceCast.assign(
                1L, 2L, 3L, CastType.PRIMARY, 0);

        assertThatThrownBy(() -> cast.change(
                3L, CastType.PRIMARY, 0))
                .isInstanceOf(InvalidPerformanceContentException.class);
    }
}
