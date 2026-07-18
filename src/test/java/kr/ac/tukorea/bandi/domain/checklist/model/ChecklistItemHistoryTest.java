package kr.ac.tukorea.bandi.domain.checklist.model;

import kr.ac.tukorea.bandi.domain.checklist.exception.InvalidChecklistItemException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChecklistItemHistoryTest {

    private static final LocalDateTime NOW =
            LocalDateTime.of(2026, 11, 21, 17, 0);

    @Test
    void 완료_취소_전후_값과_사유를_기록한다() {
        ChecklistItemHistory history = ChecklistItemHistory.change(
                10L, true, false, 20L, NOW, "장비 재확인");

        assertThat(history.isPreviousCompleted()).isTrue();
        assertThat(history.isNewCompleted()).isFalse();
        assertThat(history.getReason()).isEqualTo("장비 재확인");
    }

    @Test
    void 전후_완료_값이_같으면_이력이_아니다() {
        assertThatThrownBy(() -> ChecklistItemHistory.change(
                10L, true, true, 20L, NOW, null))
                .isInstanceOf(InvalidChecklistItemException.class);
    }

    @Test
    void 이력_식별자와_처리_시각을_검증한다() {
        assertThatThrownBy(() -> ChecklistItemHistory.change(
                null, false, true, 20L, NOW, null))
                .isInstanceOf(InvalidChecklistItemException.class);
        assertThatThrownBy(() -> ChecklistItemHistory.change(
                10L, false, true, 20L, null, null))
                .isInstanceOf(InvalidChecklistItemException.class);
    }
}
