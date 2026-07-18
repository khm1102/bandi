package kr.ac.tukorea.bandi.domain.performance.model;

import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPerformanceContentException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PerformanceRoundCastTest {

    @Test
    void 회차의_실제_출연자를_배정한다() {
        PerformanceRoundCast cast = PerformanceRoundCast.assign(
                10L, 20L, 30L, 40L, CastType.PRIMARY);

        assertThat(cast.getPerformanceProjectId()).isEqualTo(10L);
        assertThat(cast.getPerformanceRoundId()).isEqualTo(20L);
        assertThat(cast.getPublicProfileId()).isEqualTo(40L);
    }

    @Test
    void 회차_캐스팅의_식별자와_유형을_검증한다() {
        assertThatThrownBy(() -> PerformanceRoundCast.assign(
                10L, null, 30L, 40L, CastType.PRIMARY))
                .isInstanceOf(InvalidPerformanceContentException.class);
        assertThatThrownBy(() -> PerformanceRoundCast.assign(
                10L, 20L, 30L, 40L, null))
                .isInstanceOf(InvalidPerformanceContentException.class);
    }

    @Test
    void 출연자와_유형을_교체한다() {
        PerformanceRoundCast changed = cast().change(
                41L, CastType.ALTERNATE);

        assertThat(changed.getPublicProfileId()).isEqualTo(41L);
        assertThat(changed.getCastType()).isEqualTo(CastType.ALTERNATE);
    }

    @Test
    void 같은_출연자와_유형으로는_교체하지_않는다() {
        assertThatThrownBy(() -> cast().change(
                40L, CastType.PRIMARY))
                .isInstanceOf(InvalidPerformanceContentException.class);
    }

    private PerformanceRoundCast cast() {
        return new PerformanceRoundCast(50L, 10L, 20L,
                30L, 40L, CastType.PRIMARY, null, null);
    }
}
