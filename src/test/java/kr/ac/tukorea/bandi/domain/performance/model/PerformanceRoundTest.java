package kr.ac.tukorea.bandi.domain.performance.model;

import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPerformanceContentException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PerformanceRoundTest {

    private static final Long PROJECT_ID = 10L;
    private static final LocalDateTime RESERVATION_OPEN =
            LocalDateTime.of(2026, 11, 1, 10, 0);
    private static final LocalDateTime RESERVATION_CLOSE =
            LocalDateTime.of(2026, 11, 20, 18, 0);
    private static final LocalDateTime ENTRY_START =
            LocalDateTime.of(2026, 11, 21, 18, 30);
    private static final LocalDateTime START =
            LocalDateTime.of(2026, 11, 21, 19, 0);

    @Test
    void 공연_회차를_SCHEDULED_상태로_생성한다() {
        PerformanceRound round = scheduled();

        assertThat(round.getStatus())
                .isEqualTo(PerformanceRoundStatus.SCHEDULED);
        assertThat(round.getRoundNo()).isEqualTo(1);
    }

    @Test
    void 회차_번호와_필수_시각을_검증한다() {
        assertThatThrownBy(() -> PerformanceRound.scheduled(
                PROJECT_ID, 0, START, ENTRY_START,
                RESERVATION_OPEN, RESERVATION_CLOSE))
                .isInstanceOf(InvalidPerformanceContentException.class);
        assertThatThrownBy(() -> PerformanceRound.scheduled(
                PROJECT_ID, 1, null, ENTRY_START,
                RESERVATION_OPEN, RESERVATION_CLOSE))
                .isInstanceOf(InvalidPerformanceContentException.class);
    }

    @Test
    void 예약과_입장_시각은_공연_시작_이전이어야_한다() {
        assertThatThrownBy(() -> PerformanceRound.scheduled(
                PROJECT_ID, 1, START, ENTRY_START,
                RESERVATION_CLOSE, RESERVATION_OPEN))
                .isInstanceOf(InvalidPerformanceContentException.class);
        assertThatThrownBy(() -> PerformanceRound.scheduled(
                PROJECT_ID, 1, START, ENTRY_START,
                RESERVATION_OPEN, START.plusMinutes(1)))
                .isInstanceOf(InvalidPerformanceContentException.class);
        assertThatThrownBy(() -> PerformanceRound.scheduled(
                PROJECT_ID, 1, START, START.plusMinutes(1),
                RESERVATION_OPEN, RESERVATION_CLOSE))
                .isInstanceOf(InvalidPerformanceContentException.class);
    }

    @Test
    void 기본_정보를_수정해도_프로젝트와_상태는_유지한다() {
        PerformanceRound changed = scheduled().edit(
                2, START.plusDays(1), ENTRY_START.plusDays(1),
                RESERVATION_OPEN, RESERVATION_CLOSE);

        assertThat(changed.getPerformanceProjectId()).isEqualTo(PROJECT_ID);
        assertThat(changed.getRoundNo()).isEqualTo(2);
        assertThat(changed.getStatus())
                .isEqualTo(PerformanceRoundStatus.SCHEDULED);
    }

    @Test
    void 회차_상태를_변경하고_같은_상태로는_변경하지_않는다() {
        PerformanceRound opened = scheduled().changeStatus(
                PerformanceRoundStatus.RESERVATION_OPEN);

        assertThat(opened.getStatus())
                .isEqualTo(PerformanceRoundStatus.RESERVATION_OPEN);
        assertThatThrownBy(() -> opened.changeStatus(
                PerformanceRoundStatus.RESERVATION_OPEN))
                .isInstanceOf(InvalidPerformanceContentException.class);
    }

    @Test
    void 다른_프로젝트의_회차로_취급할_수_없다() {
        assertThatThrownBy(() -> scheduled().validateProject(20L))
                .isInstanceOf(InvalidPerformanceContentException.class);
    }

    private PerformanceRound scheduled() {
        return PerformanceRound.scheduled(PROJECT_ID, 1, START,
                ENTRY_START, RESERVATION_OPEN, RESERVATION_CLOSE);
    }
}
