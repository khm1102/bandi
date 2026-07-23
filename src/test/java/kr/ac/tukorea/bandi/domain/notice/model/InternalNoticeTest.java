package kr.ac.tukorea.bandi.domain.notice.model;

import kr.ac.tukorea.bandi.domain.notice.exception.InvalidInternalNoticeException;
import kr.ac.tukorea.bandi.domain.notice.exception.InvalidInternalNoticeStateException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InternalNoticeTest {

    private static final Long ACTOR_ID = 1L;
    private static final Long EDITOR_ID = 2L;
    private static final Long TEAM_ID = 4L;
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 20, 12, 0);

    @Test
    void 전체_공지_초안은_팀이_없어야_한다() {
        InternalNotice notice = InternalNotice.draft(InternalNoticeTargetScope.ALL,
                null, "전체 공지", "공지 본문", true, ACTOR_ID);

        assertThat(notice.getTargetScope()).isEqualTo(InternalNoticeTargetScope.ALL);
        assertThat(notice.getTeamId()).isNull();
        assertThat(notice.getStatus()).isEqualTo(InternalNoticeStatus.DRAFT);
        assertThat(notice.getCreatedByMemberId()).isEqualTo(ACTOR_ID);
    }

    @Test
    void 팀_공지_초안은_팀이_필수다() {
        InternalNotice notice = InternalNotice.draft(InternalNoticeTargetScope.TEAM,
                TEAM_ID, "무대팀 공지", "공지 본문", false, ACTOR_ID);

        assertThat(notice.getTargetScope()).isEqualTo(InternalNoticeTargetScope.TEAM);
        assertThat(notice.getTeamId()).isEqualTo(TEAM_ID);
    }

    @Test
    void 전체_공지에_팀을_지정하거나_팀_공지에서_팀을_비울_수_없다() {
        assertThatThrownBy(() -> InternalNotice.draft(InternalNoticeTargetScope.ALL,
                TEAM_ID, "전체 공지", "본문", false, ACTOR_ID))
                .isInstanceOf(InvalidInternalNoticeException.class);
        assertThatThrownBy(() -> InternalNotice.draft(InternalNoticeTargetScope.TEAM,
                null, "팀 공지", "본문", false, ACTOR_ID))
                .isInstanceOf(InvalidInternalNoticeException.class);
    }

    @Test
    void 제목과_본문은_필수이고_제목은_200자를_넘을_수_없다() {
        assertThatThrownBy(() -> InternalNotice.draft(InternalNoticeTargetScope.ALL,
                null, " ", "본문", false, ACTOR_ID))
                .isInstanceOf(InvalidInternalNoticeException.class);
        assertThatThrownBy(() -> InternalNotice.draft(InternalNoticeTargetScope.ALL,
                null, "제목", " ", false, ACTOR_ID))
                .isInstanceOf(InvalidInternalNoticeException.class);
        assertThatThrownBy(() -> InternalNotice.draft(InternalNoticeTargetScope.ALL,
                null, "가".repeat(201), "본문", false, ACTOR_ID))
                .isInstanceOf(InvalidInternalNoticeException.class);
    }

    @Test
    void 시작_시각이_없으면_즉시_게시하고_미래이면_예약한다() {
        InternalNotice published = teamDraft().publish(
                null, NOW.plusDays(7), ACTOR_ID, NOW);
        InternalNotice scheduled = teamDraft().publish(
                NOW.plusDays(1), NOW.plusDays(7), ACTOR_ID, NOW);

        assertThat(published.getStatus()).isEqualTo(InternalNoticeStatus.PUBLISHED);
        assertThat(published.getPublishStartDttm()).isEqualTo(NOW);
        assertThat(scheduled.getStatus()).isEqualTo(InternalNoticeStatus.SCHEDULED);
    }

    @Test
    void 게시_종료는_시작보다_빠를_수_없다() {
        assertThatThrownBy(() -> teamDraft().publish(
                NOW.plusDays(1), NOW, ACTOR_ID, NOW))
                .isInstanceOf(InvalidInternalNoticeException.class);
    }

    @Test
    void 게시_시각이_도달하고_종료되지_않은_공지만_공개된다() {
        InternalNotice published = teamDraft().publish(
                null, NOW.plusDays(1), ACTOR_ID, NOW);
        InternalNotice scheduled = teamDraft().publish(
                NOW.plusHours(1), NOW.plusDays(1), ACTOR_ID, NOW);

        assertThat(published.isPubliclyVisible(NOW)).isTrue();
        assertThat(published.isPubliclyVisible(NOW.plusDays(1))).isFalse();
        assertThat(scheduled.isPubliclyVisible(NOW)).isFalse();
        assertThat(scheduled.isPubliclyVisible(NOW.plusHours(1))).isTrue();
    }

    @Test
    void 예약_또는_게시_상태만_종료할_수_있다() {
        InternalNotice closed = teamDraft().publish(
                        null, null, ACTOR_ID, NOW)
                .close(EDITOR_ID);

        assertThat(closed.getStatus()).isEqualTo(InternalNoticeStatus.CLOSED);
        assertThat(closed.getUpdatedByMemberId()).isEqualTo(EDITOR_ID);
        assertThatThrownBy(() -> teamDraft().close(EDITOR_ID))
                .isInstanceOf(InvalidInternalNoticeStateException.class);
    }

    @Test
    void 종료하거나_보관한_공지는_수정하거나_다시_게시할_수_없다() {
        InternalNotice closed = teamDraft().publish(null, null, ACTOR_ID, NOW)
                .close(EDITOR_ID);
        InternalNotice archived = teamDraft().archive(EDITOR_ID);

        assertThatThrownBy(() -> closed.edit(InternalNoticeTargetScope.TEAM,
                TEAM_ID, "수정", "수정 본문", false, EDITOR_ID))
                .isInstanceOf(InvalidInternalNoticeStateException.class);
        assertThatThrownBy(() -> archived.edit(InternalNoticeTargetScope.TEAM,
                TEAM_ID, "수정", "수정 본문", false, EDITOR_ID))
                .isInstanceOf(InvalidInternalNoticeStateException.class);
        assertThatThrownBy(() -> archived.publish(null, null, EDITOR_ID, NOW))
                .isInstanceOf(InvalidInternalNoticeStateException.class);
    }

    @Test
    void 수정하면_식별자와_작성자와_게시정보를_유지한다() {
        InternalNotice original = persistedDraft().publish(
                null, NOW.plusDays(7), ACTOR_ID, NOW);

        InternalNotice changed = original.edit(InternalNoticeTargetScope.ALL,
                null, "전체 일정 변경", "수정 본문", true, EDITOR_ID);

        assertThat(changed.getInternalNoticeId()).isEqualTo(10L);
        assertThat(changed.getCreatedByMemberId()).isEqualTo(ACTOR_ID);
        assertThat(changed.getUpdatedByMemberId()).isEqualTo(EDITOR_ID);
        assertThat(changed.getPublishedByMemberId()).isEqualTo(ACTOR_ID);
        assertThat(changed.getTargetScope()).isEqualTo(InternalNoticeTargetScope.ALL);
        assertThat(changed.getTeamId()).isNull();
    }

    private InternalNotice teamDraft() {
        return InternalNotice.draft(InternalNoticeTargetScope.TEAM, TEAM_ID,
                "무대팀 공지", "공지 본문", false, ACTOR_ID);
    }

    private InternalNotice persistedDraft() {
        return new InternalNotice(10L, InternalNoticeTargetScope.TEAM, TEAM_ID,
                "무대팀 공지", "공지 본문", InternalNoticeStatus.DRAFT, false,
                null, null, ACTOR_ID, ACTOR_ID, null,
                NOW.minusDays(1), NOW.minusDays(1), null);
    }
}
