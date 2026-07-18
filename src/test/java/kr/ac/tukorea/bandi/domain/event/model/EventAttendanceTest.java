package kr.ac.tukorea.bandi.domain.event.model;

import kr.ac.tukorea.bandi.domain.event.exception.InvalidEventAttendanceException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventAttendanceTest {

    private static final Long EVENT_ID = 1L;
    private static final Long MEMBER_ID = 2L;
    private static final Long ACTOR_ID = 3L;
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 1, 18, 0);

    @Test
    void 행사_대상은_PENDING으로_생성한다() {
        EventAttendance attendance = EventAttendance.pending(EVENT_ID, MEMBER_ID);

        assertThat(attendance.getStatus()).isEqualTo(AttendanceStatus.PENDING);
        assertThat(attendance.getProcessedDttm()).isNull();
    }

    @Test
    void 출석_결과와_처리자를_기록하고_다른_결과로_정정한다() {
        EventAttendance present = EventAttendance.pending(EVENT_ID, MEMBER_ID)
                .changeStatus(AttendanceStatus.PRESENT, ACTOR_ID, NOW, null);
        EventAttendance late = present.changeStatus(
                AttendanceStatus.LATE, ACTOR_ID, NOW.plusMinutes(1), "지각 정정");

        assertThat(late.getStatus()).isEqualTo(AttendanceStatus.LATE);
        assertThat(late.getProcessedByMemberId()).isEqualTo(ACTOR_ID);
        assertThat(late.getReason()).isEqualTo("지각 정정");
    }

    @Test
    void 인정_결석은_사유가_필수다() {
        assertThatThrownBy(() -> EventAttendance.pending(EVENT_ID, MEMBER_ID)
                .changeStatus(AttendanceStatus.EXCUSED, ACTOR_ID, NOW, " "))
                .isInstanceOf(InvalidEventAttendanceException.class);
        assertThatThrownBy(() -> EventAttendance.pending(EVENT_ID, MEMBER_ID)
                .changeStatus(AttendanceStatus.EXCUSED, ACTOR_ID, NOW,
                        "가".repeat(501)))
                .isInstanceOf(InvalidEventAttendanceException.class);
    }

    @Test
    void PENDING으로_되돌리거나_같은_상태로_변경할_수_없다() {
        EventAttendance present = EventAttendance.pending(EVENT_ID, MEMBER_ID)
                .changeStatus(AttendanceStatus.PRESENT, ACTOR_ID, NOW, null);

        assertThatThrownBy(() -> present.changeStatus(
                AttendanceStatus.PENDING, ACTOR_ID, NOW, null))
                .isInstanceOf(InvalidEventAttendanceException.class);
        assertThatThrownBy(() -> present.changeStatus(
                AttendanceStatus.PRESENT, ACTOR_ID, NOW, null))
                .isInstanceOf(InvalidEventAttendanceException.class);
    }
}
