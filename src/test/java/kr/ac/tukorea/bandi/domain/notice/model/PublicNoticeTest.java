package kr.ac.tukorea.bandi.domain.notice.model;

import kr.ac.tukorea.bandi.domain.notice.exception.InvalidPublicNoticeException;
import kr.ac.tukorea.bandi.domain.notice.exception.InvalidPublicNoticeStateException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PublicNoticeTest {

    private static final Long CREATOR_ID = 1L;
    private static final Long EDITOR_ID = 2L;
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 20, 12, 0);

    @Test
    void 초안을_생성하면_작성자와_수정자를_기록한다() {
        PublicNotice notice = PublicNotice.draft(
                "RECRUITMENT", "신입 부원 모집", "모집 안내 본문", true, CREATOR_ID);

        assertThat(notice.getStatus()).isEqualTo(PublicNoticeStatus.DRAFT);
        assertThat(notice.getCreatedByMemberId()).isEqualTo(CREATOR_ID);
        assertThat(notice.getUpdatedByMemberId()).isEqualTo(CREATOR_ID);
        assertThat(notice.getPublishedByMemberId()).isNull();
        assertThat(notice.getPublishStartDttm()).isNull();
    }

    @Test
    void 카테고리와_제목과_본문은_필수다() {
        assertThatThrownBy(() -> PublicNotice.draft(
                " ", "신입 부원 모집", "본문", false, CREATOR_ID))
                .isInstanceOf(InvalidPublicNoticeException.class);
        assertThatThrownBy(() -> PublicNotice.draft(
                "RECRUITMENT", " ", "본문", false, CREATOR_ID))
                .isInstanceOf(InvalidPublicNoticeException.class);
        assertThatThrownBy(() -> PublicNotice.draft(
                "RECRUITMENT", "제목", " ", false, CREATOR_ID))
                .isInstanceOf(InvalidPublicNoticeException.class);
    }

    @Test
    void 카테고리는_30자_제목은_200자를_넘을_수_없다() {
        assertThatThrownBy(() -> PublicNotice.draft(
                "A".repeat(31), "제목", "본문", false, CREATOR_ID))
                .isInstanceOf(InvalidPublicNoticeException.class);
        assertThatThrownBy(() -> PublicNotice.draft(
                "RECRUITMENT", "가".repeat(201), "본문", false, CREATOR_ID))
                .isInstanceOf(InvalidPublicNoticeException.class);
    }

    @Test
    void 시작_시각이_없거나_현재이면_즉시_게시한다() {
        PublicNotice withoutStart = draft().publish(null, NOW.plusDays(7), CREATOR_ID, NOW);
        PublicNotice currentStart = draft().publish(NOW, null, CREATOR_ID, NOW);

        assertThat(withoutStart.getStatus()).isEqualTo(PublicNoticeStatus.PUBLISHED);
        assertThat(withoutStart.getPublishStartDttm()).isEqualTo(NOW);
        assertThat(withoutStart.getPublishedByMemberId()).isEqualTo(CREATOR_ID);
        assertThat(currentStart.getStatus()).isEqualTo(PublicNoticeStatus.PUBLISHED);
    }

    @Test
    void 시작_시각이_미래이면_예약_게시한다() {
        LocalDateTime future = NOW.plusDays(1);

        PublicNotice notice = draft().publish(future, future.plusDays(7), CREATOR_ID, NOW);

        assertThat(notice.getStatus()).isEqualTo(PublicNoticeStatus.SCHEDULED);
        assertThat(notice.getPublishStartDttm()).isEqualTo(future);
        assertThat(notice.getPublishedByMemberId()).isEqualTo(CREATOR_ID);
    }

    @Test
    void 게시_종료는_시작보다_빠를_수_없다() {
        assertThatThrownBy(() -> draft().publish(
                NOW.plusDays(1), NOW, CREATOR_ID, NOW))
                .isInstanceOf(InvalidPublicNoticeException.class);
    }

    @Test
    void 게시_중이거나_시작_시각이_지난_예약_공시만_공개된다() {
        PublicNotice published = draft().publish(null, NOW.plusDays(1), CREATOR_ID, NOW);
        PublicNotice scheduled = draft().publish(
                NOW.plusHours(1), NOW.plusDays(1), CREATOR_ID, NOW);

        assertThat(published.isPubliclyVisible(NOW)).isTrue();
        assertThat(scheduled.isPubliclyVisible(NOW)).isFalse();
        assertThat(scheduled.isPubliclyVisible(NOW.plusHours(1))).isTrue();
    }

    @Test
    void 임시_만료_종료_보관_공시는_공개되지_않는다() {
        PublicNotice expired = draft().publish(null, NOW.plusHours(1), CREATOR_ID, NOW);
        PublicNotice closed = draft().publish(null, null, CREATOR_ID, NOW)
                .close(EDITOR_ID);
        PublicNotice archived = draft().archive(EDITOR_ID);

        assertThat(draft().isPubliclyVisible(NOW)).isFalse();
        assertThat(expired.isPubliclyVisible(NOW.plusHours(1))).isFalse();
        assertThat(closed.isPubliclyVisible(NOW)).isFalse();
        assertThat(archived.isPubliclyVisible(NOW)).isFalse();
    }

    @Test
    void 예약_또는_게시_상태만_게시_종료할_수_있다() {
        PublicNotice published = draft().publish(null, null, CREATOR_ID, NOW);

        PublicNotice closed = published.close(EDITOR_ID);

        assertThat(closed.getStatus()).isEqualTo(PublicNoticeStatus.CLOSED);
        assertThat(closed.getUpdatedByMemberId()).isEqualTo(EDITOR_ID);
        assertThatThrownBy(() -> draft().close(EDITOR_ID))
                .isInstanceOf(InvalidPublicNoticeStateException.class);
    }

    @Test
    void 보관한_공시는_수정하거나_다시_게시할_수_없다() {
        PublicNotice archived = draft().archive(EDITOR_ID);

        assertThatThrownBy(() -> archived.edit(
                "PERFORMANCE", "수정 제목", "수정 본문", false, EDITOR_ID))
                .isInstanceOf(InvalidPublicNoticeStateException.class);
        assertThatThrownBy(() -> archived.publish(null, null, EDITOR_ID, NOW))
                .isInstanceOf(InvalidPublicNoticeStateException.class);
    }

    @Test
    void 수정하면_식별자와_작성자와_게시_정보를_유지하고_수정자만_바꾼다() {
        PublicNotice published = persistedDraft().publish(null, NOW.plusDays(7), CREATOR_ID, NOW);

        PublicNotice edited = published.edit(
                "PERFORMANCE", "정기 공연 안내", "수정된 본문", false, EDITOR_ID);

        assertThat(edited.getPublicNoticeId()).isEqualTo(10L);
        assertThat(edited.getCreatedByMemberId()).isEqualTo(CREATOR_ID);
        assertThat(edited.getUpdatedByMemberId()).isEqualTo(EDITOR_ID);
        assertThat(edited.getPublishedByMemberId()).isEqualTo(CREATOR_ID);
        assertThat(edited.getPublishStartDttm()).isEqualTo(NOW);
        assertThat(edited.getStatus()).isEqualTo(PublicNoticeStatus.PUBLISHED);
    }

    private PublicNotice draft() {
        return PublicNotice.draft(
                "RECRUITMENT", "신입 부원 모집", "모집 안내 본문", true, CREATOR_ID);
    }

    private PublicNotice persistedDraft() {
        return new PublicNotice(10L, "RECRUITMENT", "신입 부원 모집", "모집 안내 본문",
                PublicNoticeStatus.DRAFT, true, null, null, CREATOR_ID, CREATOR_ID,
                null, NOW.minusDays(1), NOW.minusDays(1), null);
    }
}
