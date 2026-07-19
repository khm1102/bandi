package kr.ac.tukorea.bandi.domain.activity.model;

import kr.ac.tukorea.bandi.domain.activity.exception.InvalidActivityRecordException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ActivityRecordRevisionTest {

    private static final LocalDateTime NOW =
            LocalDateTime.of(2026, 7, 20, 20, 0);

    @Test
    void 제출_시점의_본문을_revision으로_고정한다() {
        ActivityRecord record = persistedDraft();

        ActivityRecordRevision revision = ActivityRecordRevision.snapshot(
                record, 2, 1L, NOW, "보완 재제출");

        assertThat(revision.getRevisionNo()).isEqualTo(2);
        assertThat(revision.getTitle()).isEqualTo("1막 연습");
        assertThat(revision.getParticipantCount()).isEqualTo(8);
    }

    @Test
    void revision과_처리자와_시각은_필수다() {
        ActivityRecord record = persistedDraft();

        assertThatThrownBy(() -> ActivityRecordRevision.snapshot(
                record, 0, 1L, NOW, null))
                .isInstanceOf(InvalidActivityRecordException.class);
        assertThatThrownBy(() -> ActivityRecordRevision.snapshot(
                record, 1, null, NOW, null))
                .isInstanceOf(InvalidActivityRecordException.class);
    }

    private ActivityRecord persistedDraft() {
        return new ActivityRecord(1L, 4L, NOW.minusHours(2), "1막 연습",
                "런스루", 8, ActivityRecordStatus.DRAFT, 1L, 1L,
                null, null, null, NOW.minusHours(1), NOW.minusHours(1), null);
    }
}
