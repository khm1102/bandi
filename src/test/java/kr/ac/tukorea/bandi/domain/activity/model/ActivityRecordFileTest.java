package kr.ac.tukorea.bandi.domain.activity.model;

import kr.ac.tukorea.bandi.domain.activity.exception.InvalidActivityRecordException;
import kr.ac.tukorea.bandi.domain.activity.exception.InvalidActivityRecordStateException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ActivityRecordFileTest {

    private static final LocalDateTime NOW =
            LocalDateTime.of(2026, 7, 20, 20, 0);

    @Test
    void 현재_증빙_파일을_연결한다() {
        ActivityRecordFile file = ActivityRecordFile.create(
                1L, 2L, ActivityFileRole.EVIDENCE, 0, 3L);

        assertThat(file.isCurrent()).isTrue();
        assertThat(file.getFileRole()).isEqualTo(ActivityFileRole.EVIDENCE);
    }

    @Test
    void 새_파일로_교체하면_기존_연결에_대상과_시각과_처리자를_남긴다() {
        ActivityRecordFile original = ActivityRecordFile.create(
                1L, 2L, ActivityFileRole.EVIDENCE, 0, 3L);
        ActivityRecordFile replacement = ActivityRecordFile.create(
                1L, 4L, ActivityFileRole.EVIDENCE, 0, 3L);
        assignFileId(replacement, 10L);

        ActivityRecordFile replaced = original.markReplaced(
                replacement.getActivityRecordFileId(), 5L, NOW);

        assertThat(replaced.isCurrent()).isFalse();
        assertThat(replaced.getReplacedByActivityRecordFileId()).isEqualTo(10L);
        assertThat(replaced.getReplacedByMemberId()).isEqualTo(5L);
        assertThat(replaced.getReplacedDttm()).isEqualTo(NOW);
    }

    @Test
    void 이미_교체된_연결을_다시_교체할_수_없다() {
        ActivityRecordFile original = ActivityRecordFile.create(
                1L, 2L, ActivityFileRole.EVIDENCE, 0, 3L);
        ActivityRecordFile replaced = original.markReplaced(10L, 5L, NOW);

        assertThatThrownBy(() -> replaced.markReplaced(11L, 5L, NOW.plusMinutes(1)))
                .isInstanceOf(InvalidActivityRecordStateException.class);
    }

    @Test
    void 파일_연결의_식별자와_표시순서를_검증한다() {
        assertThatThrownBy(() -> ActivityRecordFile.create(
                null, 2L, ActivityFileRole.EVIDENCE, 0, 3L))
                .isInstanceOf(InvalidActivityRecordException.class);
        assertThatThrownBy(() -> ActivityRecordFile.create(
                1L, 2L, null, 0, 3L))
                .isInstanceOf(InvalidActivityRecordException.class);
        assertThatThrownBy(() -> ActivityRecordFile.create(
                1L, 2L, ActivityFileRole.ADDITIONAL, -1, 3L))
                .isInstanceOf(InvalidActivityRecordException.class);
    }

    private void assignFileId(ActivityRecordFile file, Long fileId) {
        try {
            Field field = ActivityRecordFile.class
                    .getDeclaredField("activityRecordFileId");
            field.setAccessible(true);
            field.set(file, fileId);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
