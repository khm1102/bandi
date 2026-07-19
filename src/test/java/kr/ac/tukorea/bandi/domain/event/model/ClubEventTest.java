package kr.ac.tukorea.bandi.domain.event.model;

import kr.ac.tukorea.bandi.domain.event.exception.InvalidClubEventException;
import kr.ac.tukorea.bandi.domain.event.exception.InvalidClubEventStateException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClubEventTest {

    private static final Long ACTOR_ID = 1L;
    private static final Long TEAM_ID = 4L;
    private static final LocalDateTime START = LocalDateTime.of(2026, 8, 1, 18, 0);
    private static final LocalDateTime END = START.plusHours(3);
    private static final LocalDateTime CHECK_IN_START = START.minusMinutes(30);
    private static final LocalDateTime CHECK_IN_END = START.plusMinutes(30);

    @Test
    void 전체_행사_초안을_작성한다() {
        ClubEvent event = draft(EventTargetScope.ALL, null);

        assertThat(event.getStatus()).isEqualTo(ClubEventStatus.DRAFT);
        assertThat(event.getTitle()).isEqualTo("여름 총회");
    }

    @Test
    void TEAM만_팀이_필수이고_ALL과_SELECTED는_팀을_둘_수_없다() {
        assertThat(draft(EventTargetScope.TEAM, TEAM_ID).getTeamId())
                .isEqualTo(TEAM_ID);
        assertThatThrownBy(() -> draft(EventTargetScope.TEAM, null))
                .isInstanceOf(InvalidClubEventException.class);
        assertThatThrownBy(() -> draft(EventTargetScope.ALL, TEAM_ID))
                .isInstanceOf(InvalidClubEventException.class);
        assertThatThrownBy(() -> draft(EventTargetScope.SELECTED, TEAM_ID))
                .isInstanceOf(InvalidClubEventException.class);
    }

    @Test
    void 행사와_출석확인_시간_범위와_필수값을_검증한다() {
        assertThatThrownBy(() -> ClubEvent.draft(null, EventTargetScope.ALL,
                null, " ", null, "학생회관", START, END,
                CHECK_IN_START, CHECK_IN_END, ACTOR_ID))
                .isInstanceOf(InvalidClubEventException.class);
        assertThatThrownBy(() -> ClubEvent.draft(null, EventTargetScope.ALL,
                null, "총회", null, "학생회관", END, START,
                CHECK_IN_START, CHECK_IN_END, ACTOR_ID))
                .isInstanceOf(InvalidClubEventException.class);
        assertThatThrownBy(() -> ClubEvent.draft(null, EventTargetScope.ALL,
                null, "총회", null, "학생회관", START, END,
                CHECK_IN_END, CHECK_IN_START, ACTOR_ID))
                .isInstanceOf(InvalidClubEventException.class);
    }

    @Test
    void 초안만_수정하고_대상_확정할_수_있다() {
        ClubEvent edited = draft(EventTargetScope.ALL, null).edit(
                EventTargetScope.TEAM, TEAM_ID, "무대팀 회의", null,
                "연습실", START, END, CHECK_IN_START, CHECK_IN_END, ACTOR_ID);
        ClubEvent scheduled = edited.schedule(ACTOR_ID);

        assertThat(edited.getTeamId()).isEqualTo(TEAM_ID);
        assertThat(scheduled.getStatus()).isEqualTo(ClubEventStatus.SCHEDULED);
        assertThatThrownBy(() -> scheduled.schedule(ACTOR_ID))
                .isInstanceOf(InvalidClubEventStateException.class);
        assertThatThrownBy(() -> scheduled.edit(EventTargetScope.ALL, null,
                "변경", null, "장소", START, END,
                CHECK_IN_START, CHECK_IN_END, ACTOR_ID))
                .isInstanceOf(InvalidClubEventStateException.class);
    }

    @Test
    void 설정된_시간_안에서만_출석확인을_연다() {
        ClubEvent scheduled = draft(EventTargetScope.ALL, null).schedule(ACTOR_ID);

        assertThatThrownBy(() -> scheduled.openCheckIn(
                ACTOR_ID, CHECK_IN_START.minusNanos(1)))
                .isInstanceOf(InvalidClubEventStateException.class);
        assertThat(scheduled.openCheckIn(ACTOR_ID, CHECK_IN_START).getStatus())
                .isEqualTo(ClubEventStatus.IN_PROGRESS);
        assertThatThrownBy(() -> scheduled.openCheckIn(
                ACTOR_ID, CHECK_IN_END.plusNanos(1)))
                .isInstanceOf(InvalidClubEventStateException.class);
    }

    @Test
    void 출석확인을_조기_종료하고_시간_안에_재개한다() {
        ClubEvent opened = draft(EventTargetScope.ALL, null).schedule(ACTOR_ID)
                .openCheckIn(ACTOR_ID, START);
        ClubEvent closed = opened.closeCheckIn(ACTOR_ID);

        assertThat(closed.getStatus()).isEqualTo(ClubEventStatus.CLOSED);
        assertThat(closed.openCheckIn(ACTOR_ID, START.plusMinutes(10)).getStatus())
                .isEqualTo(ClubEventStatus.IN_PROGRESS);
    }

    @Test
    void 출석확인이_열린_시간에만_결과를_처리한다() {
        ClubEvent scheduled = draft(EventTargetScope.ALL, null).schedule(ACTOR_ID);
        assertThatThrownBy(() -> scheduled.validateAttendanceProcessing(START))
                .isInstanceOf(InvalidClubEventStateException.class);

        ClubEvent opened = scheduled.openCheckIn(ACTOR_ID, START);
        opened.validateAttendanceProcessing(START);
        assertThatThrownBy(() -> opened.validateAttendanceProcessing(
                CHECK_IN_END.plusNanos(1)))
                .isInstanceOf(InvalidClubEventStateException.class);
    }

    @Test
    void 보관한_행사는_다시_변경할_수_없다() {
        ClubEvent archived = draft(EventTargetScope.ALL, null).archive(ACTOR_ID);

        assertThat(archived.getStatus()).isEqualTo(ClubEventStatus.ARCHIVED);
        assertThatThrownBy(() -> archived.schedule(ACTOR_ID))
                .isInstanceOf(InvalidClubEventStateException.class);
    }

    private ClubEvent draft(EventTargetScope scope, Long teamId) {
        return ClubEvent.draft(null, scope, teamId, "여름 총회", "운영 공유",
                "학생회관", START, END, CHECK_IN_START, CHECK_IN_END, ACTOR_ID);
    }
}
