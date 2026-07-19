package kr.ac.tukorea.bandi.domain.performance.model;

import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPerformanceProjectException;
import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPerformanceProjectStateException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PerformanceProjectTest {

    private static final Long ACTOR_ID = 1L;
    private static final LocalDate START_DATE = LocalDate.of(2026, 3, 1);
    private static final LocalDate END_DATE = LocalDate.of(2026, 6, 30);

    @Test
    void 학기별_공연_프로젝트를_PLANNING으로_생성한다() {
        PerformanceProject project = planning();

        assertThat(project.getStatus())
                .isEqualTo(PerformanceProjectStatus.PLANNING);
        assertThat(project.getTitle()).isEqualTo("2026 봄 정기공연");
    }

    @Test
    void 필수_정보와_제작_기간을_검증한다() {
        assertThatThrownBy(() -> PerformanceProject.planning(
                (short) 0, "FIRST", "공연", START_DATE, END_DATE,
                "대강당", ACTOR_ID))
                .isInstanceOf(InvalidPerformanceProjectException.class);
        assertThatThrownBy(() -> PerformanceProject.planning(
                (short) 2026, " ", "공연", START_DATE, END_DATE,
                "대강당", ACTOR_ID))
                .isInstanceOf(InvalidPerformanceProjectException.class);
        assertThatThrownBy(() -> PerformanceProject.planning(
                (short) 2026, "FIRST", " ", START_DATE, END_DATE,
                "대강당", ACTOR_ID))
                .isInstanceOf(InvalidPerformanceProjectException.class);
        assertThatThrownBy(() -> PerformanceProject.planning(
                (short) 2026, "FIRST", "공연", END_DATE, START_DATE,
                "대강당", ACTOR_ID))
                .isInstanceOf(InvalidPerformanceProjectException.class);
    }

    @Test
    void PLANNING에서만_기본_정보를_수정한다() {
        PerformanceProject edited = planning().edit(
                (short) 2026, "FIRST", "수정 공연",
                START_DATE.plusDays(1), END_DATE.plusDays(1),
                "소극장", ACTOR_ID);

        assertThat(edited.getTitle()).isEqualTo("수정 공연");
        assertThatThrownBy(() -> planning()
                .changeStatus(PerformanceProjectStatus.PRODUCING, ACTOR_ID)
                .edit((short) 2026, "FIRST", "변경", START_DATE,
                        END_DATE, "소극장", ACTOR_ID))
                .isInstanceOf(InvalidPerformanceProjectStateException.class);
    }

    @Test
    void 공연_상태를_운영_순서대로_전환한다() {
        PerformanceProject project = planning();

        project = project.changeStatus(
                PerformanceProjectStatus.PRODUCING, ACTOR_ID);
        project = project.changeStatus(
                PerformanceProjectStatus.RESERVATION_OPEN, ACTOR_ID);
        project = project.changeStatus(
                PerformanceProjectStatus.PERFORMING, ACTOR_ID);
        project = project.changeStatus(
                PerformanceProjectStatus.ENDED, ACTOR_ID);
        project = project.changeStatus(
                PerformanceProjectStatus.ARCHIVED, ACTOR_ID);

        assertThat(project.getStatus())
                .isEqualTo(PerformanceProjectStatus.ARCHIVED);
    }

    @Test
    void 공연_종료_전에는_프로젝트를_취소할_수_있다() {
        PerformanceProject cancelled = planning()
                .changeStatus(PerformanceProjectStatus.PRODUCING, ACTOR_ID)
                .changeStatus(PerformanceProjectStatus.CANCELLED, ACTOR_ID);

        assertThat(cancelled.getStatus())
                .isEqualTo(PerformanceProjectStatus.CANCELLED);
    }

    @Test
    void 상태를_건너뛰거나_종료_후_취소할_수_없다() {
        assertThatThrownBy(() -> planning().changeStatus(
                PerformanceProjectStatus.PERFORMING, ACTOR_ID))
                .isInstanceOf(InvalidPerformanceProjectStateException.class);
        PerformanceProject ended = planning()
                .changeStatus(PerformanceProjectStatus.PRODUCING, ACTOR_ID)
                .changeStatus(PerformanceProjectStatus.RESERVATION_OPEN, ACTOR_ID)
                .changeStatus(PerformanceProjectStatus.PERFORMING, ACTOR_ID)
                .changeStatus(PerformanceProjectStatus.ENDED, ACTOR_ID);
        assertThatThrownBy(() -> ended.changeStatus(
                PerformanceProjectStatus.CANCELLED, ACTOR_ID))
                .isInstanceOf(InvalidPerformanceProjectStateException.class);
    }

    @Test
    void 종료_취소_보관_프로젝트의_제작_업무는_변경할_수_없다() {
        planning().validateProductionMutable();
        PerformanceProject ended = planning()
                .changeStatus(PerformanceProjectStatus.PRODUCING, ACTOR_ID)
                .changeStatus(PerformanceProjectStatus.RESERVATION_OPEN, ACTOR_ID)
                .changeStatus(PerformanceProjectStatus.PERFORMING, ACTOR_ID)
                .changeStatus(PerformanceProjectStatus.ENDED, ACTOR_ID);

        assertThatThrownBy(ended::validateProductionMutable)
                .isInstanceOf(InvalidPerformanceProjectStateException.class);
        assertThatThrownBy(() -> planning()
                .changeStatus(PerformanceProjectStatus.CANCELLED, ACTOR_ID)
                .validateProductionMutable())
                .isInstanceOf(InvalidPerformanceProjectStateException.class);
    }

    private PerformanceProject planning() {
        return PerformanceProject.planning((short) 2026, "FIRST",
                "2026 봄 정기공연", START_DATE, END_DATE,
                "대강당", ACTOR_ID);
    }
}
