package kr.ac.tukorea.bandi.domain.production.model;

import kr.ac.tukorea.bandi.domain.production.exception.InvalidProductionTaskException;
import kr.ac.tukorea.bandi.domain.production.exception.InvalidProductionTaskStateException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductionTaskTest {

    private static final Long ACTOR_ID = 1L;
    private static final LocalDate START_DATE = LocalDate.of(2026, 3, 1);
    private static final LocalDate DUE_DATE = LocalDate.of(2026, 3, 31);

    @Test
    void 팀_제작_업무를_TODO로_생성한다() {
        ProductionTask task = todo();

        assertThat(task.getStatus()).isEqualTo(ProductionTaskStatus.TODO);
        assertThat(task.getTeamId()).isEqualTo(4L);
    }

    @Test
    void 제목과_기간과_필수_참조를_검증한다() {
        assertThatThrownBy(() -> ProductionTask.todo(
                null, 4L, "업무", null, START_DATE, DUE_DATE, ACTOR_ID))
                .isInstanceOf(InvalidProductionTaskException.class);
        assertThatThrownBy(() -> ProductionTask.todo(
                10L, 4L, " ", null, START_DATE, DUE_DATE, ACTOR_ID))
                .isInstanceOf(InvalidProductionTaskException.class);
        assertThatThrownBy(() -> ProductionTask.todo(
                10L, 4L, "업무", null, DUE_DATE, START_DATE, ACTOR_ID))
                .isInstanceOf(InvalidProductionTaskException.class);
    }

    @Test
    void 기본_정보를_수정해도_현재_상태를_유지한다() {
        ProductionTask changed = todo().edit("수정 업무", "수정 설명",
                START_DATE.plusDays(1), DUE_DATE.plusDays(1), ACTOR_ID);

        assertThat(changed.getTitle()).isEqualTo("수정 업무");
        assertThat(changed.getStatus()).isEqualTo(ProductionTaskStatus.TODO);
    }

    @Test
    void BLOCKED는_차단_사유가_필수다() {
        assertThatThrownBy(() -> todo().changeStatus(
                ProductionTaskStatus.BLOCKED, " ", ACTOR_ID))
                .isInstanceOf(InvalidProductionTaskException.class);

        ProductionTask blocked = todo().changeStatus(
                ProductionTaskStatus.BLOCKED, "자재 도착 대기", ACTOR_ID);
        assertThat(blocked.getBlockedReason()).isEqualTo("자재 도착 대기");
    }

    @Test
    void 다른_상태로_변경하면_차단_사유를_제거한다() {
        ProductionTask blocked = todo().changeStatus(
                ProductionTaskStatus.BLOCKED, "자재 도착 대기", ACTOR_ID);
        ProductionTask progress = blocked.changeStatus(
                ProductionTaskStatus.IN_PROGRESS, null, ACTOR_ID);

        assertThat(progress.getBlockedReason()).isNull();
    }

    @Test
    void 같은_상태로는_변경할_수_없다() {
        assertThatThrownBy(() -> todo().changeStatus(
                ProductionTaskStatus.TODO, null, ACTOR_ID))
                .isInstanceOf(InvalidProductionTaskStateException.class);
    }

    private ProductionTask todo() {
        return ProductionTask.todo(10L, 4L, "무대 도면 확정",
                "최종 치수 반영", START_DATE, DUE_DATE, ACTOR_ID);
    }
}
