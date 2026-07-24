package kr.ac.tukorea.bandi.domain.activity.model;

import kr.ac.tukorea.bandi.domain.activity.exception.InvalidActivityRecordException;
import kr.ac.tukorea.bandi.domain.activity.exception.InvalidActivityRecordStateException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ActivityRecordTest {

    private static final Long TEAM_ID = 4L;
    private static final Long ACTOR_ID = 1L;
    private static final Long REVIEWER_ID = 2L;
    private static final LocalDateTime ACTIVITY_DTTM =
            LocalDateTime.of(2026, 7, 20, 18, 0);
    private static final LocalDateTime NOW =
            LocalDateTime.of(2026, 7, 20, 20, 0);

    @Test
    void 팀_활동_초안을_작성한다() {
        ActivityRecord record = draft();

        assertThat(record.getTeamId()).isEqualTo(TEAM_ID);
        assertThat(record.getStatus()).isEqualTo(ActivityRecordStatus.DRAFT);
        assertThat(record.getParticipantCount()).isEqualTo(8);
    }

    @Test
    void 팀과_활동시각과_제목과_본문과_참여인원을_검증한다() {
        assertThatThrownBy(() -> ActivityRecord.draft(null, ACTIVITY_DTTM,
                "연습", "연습 내용", 8, ACTOR_ID))
                .isInstanceOf(InvalidActivityRecordException.class);
        assertThatThrownBy(() -> ActivityRecord.draft(TEAM_ID, null,
                "연습", "연습 내용", 8, ACTOR_ID))
                .isInstanceOf(InvalidActivityRecordException.class);
        assertThatThrownBy(() -> ActivityRecord.draft(TEAM_ID, ACTIVITY_DTTM,
                "가".repeat(151), "연습 내용", 8, ACTOR_ID))
                .isInstanceOf(InvalidActivityRecordException.class);
        assertThatThrownBy(() -> ActivityRecord.draft(TEAM_ID, ACTIVITY_DTTM,
                "연습", " ", 8, ACTOR_ID))
                .isInstanceOf(InvalidActivityRecordException.class);
        assertThatThrownBy(() -> ActivityRecord.draft(TEAM_ID, ACTIVITY_DTTM,
                "연습", "연습 내용", 0, ACTOR_ID))
                .isInstanceOf(InvalidActivityRecordException.class);
    }

    @Test
    void 초안을_제출하면_제출시각을_기록하고_수정을_잠근다() {
        ActivityRecord submitted = draft().submit(ACTOR_ID, NOW);

        assertThat(submitted.getStatus()).isEqualTo(ActivityRecordStatus.SUBMITTED);
        assertThat(submitted.getSubmittedDttm()).isEqualTo(NOW);
        assertThatThrownBy(() -> submitted.edit(ACTIVITY_DTTM, "수정",
                "수정 내용", 9, ACTOR_ID))
                .isInstanceOf(InvalidActivityRecordStateException.class);
    }

    @Test
    void 팀장_1차_승인_뒤_관리자가_최종_승인하면_각_단계를_기록한다() {
        ActivityRecord teamApproved = draft().submit(ACTOR_ID, NOW.minusHours(2))
                .teamApprove(REVIEWER_ID, NOW.minusHours(1));
        ActivityRecord approved = teamApproved.finalApprove(3L, NOW);

        assertThat(teamApproved.getStatus()).isEqualTo(ActivityRecordStatus.TEAM_APPROVED);
        assertThat(approved.getStatus()).isEqualTo(ActivityRecordStatus.APPROVED);
        assertThat(approved.getReviewedByMemberId()).isEqualTo(3L);
        assertThat(approved.getReviewedDttm()).isEqualTo(NOW);
    }

    @Test
    void 제출_기록에_보완을_요청하면_다시_수정하고_재제출할_수_있다() {
        ActivityRecord requested = draft().submit(ACTOR_ID, NOW.minusHours(2))
                .teamApprove(REVIEWER_ID, NOW.minusHours(1))
                .requestRevision(3L, NOW.minusMinutes(30));

        ActivityRecord edited = requested.edit(ACTIVITY_DTTM.plusDays(1),
                "보완 연습", "보완 내용", 9, ACTOR_ID);
        ActivityRecord resubmitted = edited.submit(ACTOR_ID, NOW);

        assertThat(resubmitted.getStatus()).isEqualTo(ActivityRecordStatus.SUBMITTED);
        assertThat(resubmitted.getSubmittedDttm()).isEqualTo(NOW);
        assertThat(resubmitted.getReviewedByMemberId()).isNull();
        assertThat(resubmitted.getReviewedDttm()).isNull();
    }

    @Test
    void 승인이나_초안은_검수할_수_없다() {
        assertThatThrownBy(() -> draft().teamApprove(REVIEWER_ID, NOW))
                .isInstanceOf(InvalidActivityRecordStateException.class);
        ActivityRecord approved = draft().submit(ACTOR_ID, NOW.minusHours(1))
                .teamApprove(REVIEWER_ID, NOW)
                .finalApprove(3L, NOW.plusMinutes(1));
        assertThatThrownBy(() -> approved.requestRevision(REVIEWER_ID, NOW))
                .isInstanceOf(InvalidActivityRecordStateException.class);
    }

    @Test
    void 보관한_기록은_수정하거나_제출할_수_없다() {
        ActivityRecord archived = draft().submit(ACTOR_ID, NOW.minusHours(2))
                .teamApprove(REVIEWER_ID, NOW.minusHours(1))
                .finalApprove(3L, NOW)
                .archive(3L);

        assertThat(archived.getStatus()).isEqualTo(ActivityRecordStatus.ARCHIVED);
        assertThatThrownBy(() -> archived.edit(ACTIVITY_DTTM, "수정",
                "수정 내용", 8, ACTOR_ID))
                .isInstanceOf(InvalidActivityRecordStateException.class);
        assertThatThrownBy(() -> archived.submit(ACTOR_ID, NOW))
                .isInstanceOf(InvalidActivityRecordStateException.class);
    }

    private ActivityRecord draft() {
        return ActivityRecord.draft(TEAM_ID, ACTIVITY_DTTM,
                "1막 연습", "1막 전체 런스루", 8, ACTOR_ID);
    }
}
