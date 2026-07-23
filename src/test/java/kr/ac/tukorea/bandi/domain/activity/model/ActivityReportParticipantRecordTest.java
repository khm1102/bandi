package kr.ac.tukorea.bandi.domain.activity.model;

import kr.ac.tukorea.bandi.domain.activity.exception.InvalidActivityReportDocumentException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ActivityReportParticipantRecordTest {

    @Test
    void 참여자_순서와_입력값을_저장한다() {
        ActivityReportParticipantRecord record = ActivityReportParticipantRecord.create(
                4L, 2, new ActivityReportParticipant("김현민", "컴퓨터공학부",
                        "2025591010", ""));

        assertThat(record.getDisplayOrder()).isEqualTo(2);
        assertThat(record.getName()).isEqualTo("김현민");
        assertThat(record.getNote()).isNull();
    }

    @Test
    void 열네_번째를_넘는_순서는_거부한다() {
        assertThatThrownBy(() -> ActivityReportParticipantRecord.create(4L, 14,
                new ActivityReportParticipant("김현민", null, null, null)))
                .isInstanceOf(InvalidActivityReportDocumentException.class);
    }
}
