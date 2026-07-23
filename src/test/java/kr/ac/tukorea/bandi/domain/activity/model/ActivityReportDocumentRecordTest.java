package kr.ac.tukorea.bandi.domain.activity.model;

import kr.ac.tukorea.bandi.domain.activity.exception.InvalidActivityReportDocumentException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ActivityReportDocumentRecordTest {

    @Test
    void 활동_기록과_문서_입력값을_저장_모델로_만든다() {
        ActivityReportDocumentRecord record = ActivityReportDocumentRecord.create(3L,
                document());

        assertThat(record.getActivityRecordId()).isEqualTo(3L);
        assertThat(record.getRepresentative()).isEqualTo("대표자");
        assertThat(record.getLocation()).isEqualTo("종합관");
    }

    @Test
    void 활동_기록_ID가_없으면_저장_모델을_만들지_못한다() {
        assertThatThrownBy(() -> ActivityReportDocumentRecord.create(null, document()))
                .isInstanceOf(InvalidActivityReportDocumentException.class);
    }

    private ActivityReportDocument document() {
        return ActivityReportDocument.create("대표자", "종합관",
                LocalDateTime.of(2026, 7, 23, 20, 0), "활동 내용",
                List.of(new ActivityReportParticipant("참여자", null, null, null)));
    }
}
