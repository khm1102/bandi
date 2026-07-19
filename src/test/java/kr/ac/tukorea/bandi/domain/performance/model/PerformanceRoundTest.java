package kr.ac.tukorea.bandi.domain.performance.model;

import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPerformanceContentException;
import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPerformanceRoundStateException;
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
    void 회차_상태를_운영_순서대로_전환한다() {
        PerformanceRound ended = entryOpen().changeStatus(
                PerformanceRoundStatus.ENDED);

        assertThat(ended.getStatus())
                .isEqualTo(PerformanceRoundStatus.ENDED);
    }

    @Test
    void 종료_전_운영_상태에서는_회차를_취소할_수_있다() {
        assertThat(scheduled().changeStatus(
                PerformanceRoundStatus.CANCELLED).getStatus())
                .isEqualTo(PerformanceRoundStatus.CANCELLED);
        assertThat(reservationOpen().changeStatus(
                PerformanceRoundStatus.CANCELLED).getStatus())
                .isEqualTo(PerformanceRoundStatus.CANCELLED);
        assertThat(reservationClosed().changeStatus(
                PerformanceRoundStatus.CANCELLED).getStatus())
                .isEqualTo(PerformanceRoundStatus.CANCELLED);
        assertThat(entryOpen().changeStatus(
                PerformanceRoundStatus.CANCELLED).getStatus())
                .isEqualTo(PerformanceRoundStatus.CANCELLED);
    }

    @Test
    void 회차_상태를_건너뛰거나_역행하거나_같게_변경할_수_없다() {
        assertThatThrownBy(() -> scheduled().changeStatus(
                PerformanceRoundStatus.ENTRY_OPEN))
                .isInstanceOf(InvalidPerformanceRoundStateException.class);
        assertThatThrownBy(() -> reservationOpen().changeStatus(
                PerformanceRoundStatus.SCHEDULED))
                .isInstanceOf(InvalidPerformanceRoundStateException.class);
        assertThatThrownBy(() -> reservationOpen().changeStatus(
                PerformanceRoundStatus.RESERVATION_OPEN))
                .isInstanceOf(InvalidPerformanceRoundStateException.class);
        assertThatThrownBy(() -> scheduled().changeStatus(null))
                .isInstanceOf(InvalidPerformanceRoundStateException.class);
    }

    @Test
    void 종료와_취소_상태에서는_더_전환할_수_없다() {
        PerformanceRound ended = entryOpen().changeStatus(
                PerformanceRoundStatus.ENDED);
        PerformanceRound cancelled = scheduled().changeStatus(
                PerformanceRoundStatus.CANCELLED);

        assertThatThrownBy(() -> ended.changeStatus(
                PerformanceRoundStatus.CANCELLED))
                .isInstanceOf(InvalidPerformanceRoundStateException.class);
        assertThatThrownBy(() -> cancelled.changeStatus(
                PerformanceRoundStatus.SCHEDULED))
                .isInstanceOf(InvalidPerformanceRoundStateException.class);
    }

    @Test
    void 다른_프로젝트의_회차로_취급할_수_없다() {
        assertThatThrownBy(() -> scheduled().validateProject(20L))
                .isInstanceOf(InvalidPerformanceContentException.class);
    }

    @Test
    void 신청_오픈_상태이고_시작_이상_마감_미만일_때만_신청한다() {
        PerformanceRound opened = scheduled().changeStatus(
                PerformanceRoundStatus.RESERVATION_OPEN);

        assertThat(opened.isReservationOpenAt(RESERVATION_OPEN)).isTrue();
        assertThat(opened.isReservationOpenAt(
                RESERVATION_CLOSE.minusNanos(1))).isTrue();
        assertThat(opened.isReservationOpenAt(RESERVATION_CLOSE)).isFalse();
        assertThat(scheduled().isReservationOpenAt(
                RESERVATION_OPEN)).isFalse();
    }

    @Test
    void 관람객_취소는_입장_오픈_전_상태까지만_허용한다() {
        assertThat(scheduled().isViewerCancellationOpen()).isTrue();
        assertThat(reservationClosed().isViewerCancellationOpen()).isTrue();
        assertThat(entryOpen().isViewerCancellationOpen()).isFalse();
        assertThat(entryOpen().changeStatus(
                PerformanceRoundStatus.ENDED).isViewerCancellationOpen())
                .isFalse();
        assertThat(scheduled().changeStatus(
                PerformanceRoundStatus.CANCELLED)
                .isViewerCancellationOpen()).isFalse();
    }

    @Test
    void 입장은_ENTRY_OPEN_상태에서만_허용한다() {
        assertThat(scheduled().isEntryOpen()).isFalse();
        assertThat(entryOpen().isEntryOpen()).isTrue();
    }

    private PerformanceRound scheduled() {
        return PerformanceRound.scheduled(PROJECT_ID, 1, START,
                ENTRY_START, RESERVATION_OPEN, RESERVATION_CLOSE);
    }

    private PerformanceRound reservationOpen() {
        return scheduled().changeStatus(
                PerformanceRoundStatus.RESERVATION_OPEN);
    }

    private PerformanceRound reservationClosed() {
        return reservationOpen().changeStatus(
                PerformanceRoundStatus.RESERVATION_CLOSED);
    }

    private PerformanceRound entryOpen() {
        return reservationClosed().changeStatus(
                PerformanceRoundStatus.ENTRY_OPEN);
    }
}
