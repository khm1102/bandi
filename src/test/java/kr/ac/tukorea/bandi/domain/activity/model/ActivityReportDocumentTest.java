package kr.ac.tukorea.bandi.domain.activity.model;

import kr.ac.tukorea.bandi.domain.activity.exception.InvalidActivityReportDocumentException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ActivityReportDocumentTest {

    private static final LocalDateTime ACTIVITY_AT =
            LocalDateTime.of(2026, 2, 11, 16, 30);

    @Test
    void 필수_입력과_참여자_목록을_보존한다() {
        ActivityReportParticipant participant = new ActivityReportParticipant(
                "김현민", "컴퓨터공학부", "2025591010", "");

        ActivityReportDocument document = ActivityReportDocument.create(
                "김현민", "종합관 120호", ACTIVITY_AT, "대본 리딩을 진행했습니다.",
                List.of(participant));

        assertThat(document.representative()).isEqualTo("김현민");
        assertThat(document.location()).isEqualTo("종합관 120호");
        assertThat(document.activityAt()).isEqualTo(ACTIVITY_AT);
        assertThat(document.participants()).containsExactly(participant);
    }

    @Test
    void 참여자는_한_명부터_열네_명까지_허용한다() {
        List<ActivityReportParticipant> fourteen = java.util.stream.IntStream
                .rangeClosed(1, 14)
                .mapToObj(index -> new ActivityReportParticipant(
                        "참여자" + index, "학과", "202600" + index, ""))
                .toList();

        ActivityReportDocument document = ActivityReportDocument.create(
                "대표자", "장소", ACTIVITY_AT, "활동 내용", fourteen);

        assertThat(document.participants()).hasSize(14);
        assertThatThrownBy(() -> ActivityReportDocument.create(
                "대표자", "장소", ACTIVITY_AT, "활동 내용", List.of()))
                .isInstanceOf(InvalidActivityReportDocumentException.class);
        assertThatThrownBy(() -> ActivityReportDocument.create(
                "대표자", "장소", ACTIVITY_AT, "활동 내용",
                java.util.stream.Stream.concat(fourteen.stream(),
                                java.util.stream.Stream.of(fourteen.get(0)))
                        .toList()))
                .isInstanceOf(InvalidActivityReportDocumentException.class);
    }

    @Test
    void 입력별_최대_길이와_필수값을_검증한다() {
        ActivityReportParticipant participant = new ActivityReportParticipant(
                "김현민", null, null, null);

        assertThatThrownBy(() -> ActivityReportDocument.create(
                " ", "장소", ACTIVITY_AT, "내용", List.of(participant)))
                .isInstanceOf(InvalidActivityReportDocumentException.class);
        assertThatThrownBy(() -> ActivityReportDocument.create(
                "가".repeat(21), "장소", ACTIVITY_AT, "내용", List.of(participant)))
                .isInstanceOf(InvalidActivityReportDocumentException.class);
        assertThatThrownBy(() -> ActivityReportDocument.create(
                "대표", "가".repeat(51), ACTIVITY_AT, "내용", List.of(participant)))
                .isInstanceOf(InvalidActivityReportDocumentException.class);
        assertThatThrownBy(() -> ActivityReportDocument.create(
                "대표", "장소", ACTIVITY_AT, "가".repeat(301), List.of(participant)))
                .isInstanceOf(InvalidActivityReportDocumentException.class);
    }

    @Test
    void 참여자_필드의_필수값과_최대_길이를_검증한다() {
        assertThatThrownBy(() -> new ActivityReportParticipant(
                "", "학과", "학번", "비고"))
                .isInstanceOf(InvalidActivityReportDocumentException.class);
        assertThatThrownBy(() -> new ActivityReportParticipant(
                "가".repeat(21), null, null, null))
                .isInstanceOf(InvalidActivityReportDocumentException.class);
        assertThatThrownBy(() -> new ActivityReportParticipant(
                "이름", "가".repeat(31), null, null))
                .isInstanceOf(InvalidActivityReportDocumentException.class);
        assertThatThrownBy(() -> new ActivityReportParticipant(
                "이름", null, "1".repeat(21), null))
                .isInstanceOf(InvalidActivityReportDocumentException.class);
        assertThatThrownBy(() -> new ActivityReportParticipant(
                "이름", null, null, "가".repeat(41)))
                .isInstanceOf(InvalidActivityReportDocumentException.class);
    }
}
