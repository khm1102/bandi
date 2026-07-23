package kr.ac.tukorea.bandi.domain.calendar.model;

import kr.ac.tukorea.bandi.domain.calendar.exception.InvalidCalendarEventException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CalendarEventTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 7, 20, 18, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 7, 20, 21, 0);

    @Test
    void 팀_일정을_생성한다() {
        CalendarEvent event = CalendarEvent.create(4L, "무대 연습", "전체 장면 연습",
                START, END, false, "학생회관 소극장", 1L);

        assertThat(event.getTeamId()).isEqualTo(4L);
        assertThat(event.getTitle()).isEqualTo("무대 연습");
        assertThat(event.getCreatedByMemberId()).isEqualTo(1L);
        assertThat(event.getUpdatedByMemberId()).isEqualTo(1L);
    }

    @Test
    void 팀이_없으면_전체_일정으로_생성한다() {
        CalendarEvent event = CalendarEvent.create(null, "전체 리허설", "전 팀 참석",
                START, END, false, "학생회관 소극장", 1L);

        assertThat(event.getTeamId()).isNull();
    }

    @Test
    void 종료가_시작보다_빠른_일정은_생성할_수_없다() {
        assertThatThrownBy(() -> CalendarEvent.create(4L, "무대 연습", "전체 장면 연습",
                END, START, false, "학생회관 소극장", 1L))
                .isInstanceOf(InvalidCalendarEventException.class);
    }

    @Test
    void 종료가_시작과_같은_일정은_생성할_수_없다() {
        assertThatThrownBy(() -> CalendarEvent.create(4L, "무대 연습", "전체 장면 연습",
                START, START, false, "학생회관 소극장", 1L))
                .isInstanceOf(InvalidCalendarEventException.class);
    }

    @Test
    void 제목은_필수이고_150자를_넘을_수_없다() {
        CalendarEvent boundary = CalendarEvent.create(4L, "가".repeat(150), null,
                START, END, false, null, 1L);

        assertThat(boundary.getTitle()).hasSize(150);
        assertThatThrownBy(() -> CalendarEvent.create(4L, " ", "전체 장면 연습",
                START, END, false, "학생회관 소극장", 1L))
                .isInstanceOf(InvalidCalendarEventException.class);
        assertThatThrownBy(() -> CalendarEvent.create(4L, "가".repeat(151), "전체 장면 연습",
                START, END, false, "학생회관 소극장", 1L))
                .isInstanceOf(InvalidCalendarEventException.class);
    }

    @Test
    void 장소와_설명은_입력하지_않아도_된다() {
        CalendarEvent event = CalendarEvent.create(4L, "무대 연습", null,
                START, END, false, null, 1L);

        assertThat(event.getDescription()).isNull();
        assertThat(event.getPlace()).isNull();
    }

    @Test
    void 장소는_200자를_넘을_수_없다() {
        assertThatThrownBy(() -> CalendarEvent.create(4L, "무대 연습", "전체 장면 연습",
                START, END, false, "가".repeat(201), 1L))
                .isInstanceOf(InvalidCalendarEventException.class);
    }

    @Test
    void 종일_일정은_자정부터_다음날_자정까지의_배타적_종료를_사용한다() {
        LocalDateTime dayStart = LocalDateTime.of(2026, 7, 20, 0, 0);
        LocalDateTime dayEnd = LocalDateTime.of(2026, 7, 21, 0, 0);

        CalendarEvent event = CalendarEvent.create(null, "공연일", "정기공연",
                dayStart, dayEnd, true, "학생회관 소극장", 1L);

        assertThat(event.isAllDay()).isTrue();
        assertThat(event.getEndDttm()).isEqualTo(dayEnd);
    }

    @Test
    void 종일_일정의_시작과_종료는_자정이어야_한다() {
        assertThatThrownBy(() -> CalendarEvent.create(null, "공연일", null,
                START, END, true, null, 1L))
                .isInstanceOf(InvalidCalendarEventException.class);
    }

    @Test
    void 수정본은_식별자와_생성자를_유지하고_수정자를_교체한다() {
        CalendarEvent original = persisted();

        CalendarEvent changed = original.change(5L, "오퍼 연습", "음향 큐 연습",
                START.plusDays(1), END.plusDays(1), false, "대학극장", 2L);

        assertThat(changed.getCalendarEventId()).isEqualTo(10L);
        assertThat(changed.getCreatedByMemberId()).isEqualTo(1L);
        assertThat(changed.getUpdatedByMemberId()).isEqualTo(2L);
        assertThat(changed.getTeamId()).isEqualTo(5L);
    }

    private CalendarEvent persisted() {
        return new CalendarEvent(10L, 4L, "무대 연습", "전체 장면 연습",
                START, END, false, "학생회관 소극장", 1L, 1L,
                START.minusDays(1), START.minusDays(1), null);
    }
}
