package kr.ac.tukorea.bandi.domain.checklist.model;

import kr.ac.tukorea.bandi.domain.checklist.exception.InvalidChecklistItemException;
import kr.ac.tukorea.bandi.domain.checklist.exception.InvalidChecklistItemStateException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChecklistItemTest {

    private static final Long PROJECT_ID = 10L;
    private static final Long ROUND_ID = 20L;
    private static final Long TEAM_ID = 30L;
    private static final Long ACTOR_ID = 40L;
    private static final LocalDateTime NOW =
            LocalDateTime.of(2026, 11, 21, 17, 0);

    @Test
    void 프로젝트_공통_체크리스트를_미완료로_생성한다() {
        ChecklistItem item = ChecklistItem.project(
                PROJECT_ID, TEAM_ID, "무대 안전 확인",
                true, 0, ACTOR_ID);

        assertThat(item.getScope()).isEqualTo(ChecklistScope.PROJECT);
        assertThat(item.getPerformanceRoundId()).isNull();
        assertThat(item.isCompleted()).isFalse();
    }

    @Test
    void 회차별_체크리스트는_회차를_필수로_가진다() {
        ChecklistItem item = ChecklistItem.round(
                PROJECT_ID, ROUND_ID, TEAM_ID,
                "객석 입장 동선 확인", true, 0, ACTOR_ID);

        assertThat(item.getScope()).isEqualTo(ChecklistScope.ROUND);
        assertThat(item.getPerformanceRoundId()).isEqualTo(ROUND_ID);
        assertThatThrownBy(() -> ChecklistItem.round(
                PROJECT_ID, null, TEAM_ID, "확인", true, 0, ACTOR_ID))
                .isInstanceOf(InvalidChecklistItemException.class);
    }

    @Test
    void 프로젝트_범위에는_회차를_연결할_수_없다() {
        assertThatThrownBy(() -> new ChecklistItem(null,
                PROJECT_ID, ROUND_ID, TEAM_ID, ChecklistScope.PROJECT,
                "확인", true, 0, false, null, null,
                ACTOR_ID, ACTOR_ID, null, null, null))
                .isInstanceOf(InvalidChecklistItemException.class);
    }

    @Test
    void 내용과_표시_순서와_식별자를_검증한다() {
        assertThatThrownBy(() -> ChecklistItem.project(
                PROJECT_ID, TEAM_ID, " ", true, 0, ACTOR_ID))
                .isInstanceOf(InvalidChecklistItemException.class);
        assertThatThrownBy(() -> ChecklistItem.project(
                PROJECT_ID, TEAM_ID, "확인", true, -1, ACTOR_ID))
                .isInstanceOf(InvalidChecklistItemException.class);
        assertThatThrownBy(() -> ChecklistItem.project(
                PROJECT_ID, null, "확인", true, 0, ACTOR_ID))
                .isInstanceOf(InvalidChecklistItemException.class);
    }

    @Test
    void 항목을_수정해도_담당_범위와_완료_상태는_유지한다() {
        ChecklistItem completed = project().changeCompleted(
                true, ACTOR_ID, NOW);

        ChecklistItem changed = completed.edit(
                "수정된 안전 확인", false, 2, ACTOR_ID);

        assertThat(changed.getTeamId()).isEqualTo(TEAM_ID);
        assertThat(changed.getScope()).isEqualTo(ChecklistScope.PROJECT);
        assertThat(changed.isCompleted()).isTrue();
        assertThat(changed.getCompletedByMemberId()).isEqualTo(ACTOR_ID);
    }

    @Test
    void 완료하면_처리자와_시각을_기록하고_취소하면_제거한다() {
        ChecklistItem completed = project().changeCompleted(
                true, ACTOR_ID, NOW);
        ChecklistItem cancelled = completed.changeCompleted(
                false, ACTOR_ID, NOW.plusMinutes(10));

        assertThat(completed.isCompleted()).isTrue();
        assertThat(completed.getCompletedByMemberId()).isEqualTo(ACTOR_ID);
        assertThat(completed.getCompletedDttm()).isEqualTo(NOW);
        assertThat(cancelled.isCompleted()).isFalse();
        assertThat(cancelled.getCompletedByMemberId()).isNull();
        assertThat(cancelled.getCompletedDttm()).isNull();
    }

    @Test
    void 같은_완료_상태로는_변경하지_않는다() {
        assertThatThrownBy(() -> project().changeCompleted(
                false, ACTOR_ID, NOW))
                .isInstanceOf(InvalidChecklistItemStateException.class);
    }

    private ChecklistItem project() {
        return ChecklistItem.project(PROJECT_ID, TEAM_ID,
                "무대 안전 확인", true, 0, ACTOR_ID);
    }
}
