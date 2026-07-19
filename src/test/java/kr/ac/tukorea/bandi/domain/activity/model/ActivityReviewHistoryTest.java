package kr.ac.tukorea.bandi.domain.activity.model;

import kr.ac.tukorea.bandi.domain.activity.exception.InvalidActivityRecordException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ActivityReviewHistoryTest {

    private static final LocalDateTime NOW =
            LocalDateTime.of(2026, 7, 20, 20, 0);

    @Test
    void 상태_변경과_처리자를_기록한다() {
        ActivityReviewHistory history = ActivityReviewHistory.change(1L,
                ActivityRecordStatus.SUBMITTED, ActivityRecordStatus.APPROVED,
                null, 2L, NOW);

        assertThat(history.getNewStatus()).isEqualTo(ActivityRecordStatus.APPROVED);
        assertThat(history.getReviewedByMemberId()).isEqualTo(2L);
    }

    @Test
    void 보완_요청은_의견이_필수다() {
        assertThatThrownBy(() -> ActivityReviewHistory.change(1L,
                ActivityRecordStatus.SUBMITTED,
                ActivityRecordStatus.REVISION_REQUESTED, " ", 2L, NOW))
                .isInstanceOf(InvalidActivityRecordException.class);
    }

    @Test
    void 같은_상태로는_이력을_만들지_않는다() {
        assertThatThrownBy(() -> ActivityReviewHistory.change(1L,
                ActivityRecordStatus.SUBMITTED, ActivityRecordStatus.SUBMITTED,
                null, 2L, NOW))
                .isInstanceOf(InvalidActivityRecordException.class);
    }
}
