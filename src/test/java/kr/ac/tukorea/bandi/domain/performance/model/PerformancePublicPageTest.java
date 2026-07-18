package kr.ac.tukorea.bandi.domain.performance.model;

import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPerformancePublicPageException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PerformancePublicPageTest {

    private static final LocalDateTime PUBLISH_START =
            LocalDateTime.of(2026, 8, 1, 12, 0);

    @Test
    void 공개_페이지를_초안으로_생성한다() {
        PerformancePublicPage page = draft("hamlet-2026");

        assertThat(page.getStatus()).isEqualTo(PublicPageStatus.DRAFT);
        assertThat(page.getAccentColor()).isEqualTo("#0F6F5D");
        assertThat(page.getAdmissionFee()).isZero();
    }

    @Test
    void 슬러그는_영문_소문자_숫자와_단일_하이픈만_허용한다() {
        assertThatThrownBy(() -> draft("Hamlet 2026"))
                .isInstanceOf(InvalidPerformancePublicPageException.class);
        assertThatThrownBy(() -> draft("hamlet--2026"))
                .isInstanceOf(InvalidPerformancePublicPageException.class);
    }

    @Test
    void 러닝타임과_관람료와_강조색을_검증한다() {
        assertThatThrownBy(() -> PerformancePublicPage.draft(
                1L, "hamlet-2026", "소개", "시놉시스", null,
                "비극", "12세 이상", 0, null, -1L,
                null, null, "red", "문의", "channel", "Bandi",
                null, null, null, null, null))
                .isInstanceOf(InvalidPerformancePublicPageException.class);
    }

    @Test
    void 공개_예정은_시작_시각이_필수이고_종료는_시작보다_뒤여야_한다() {
        PerformancePublicPage page = draft("hamlet-2026");

        assertThatThrownBy(() -> page.changeStatus(
                PublicPageStatus.SCHEDULED))
                .isInstanceOf(InvalidPerformancePublicPageException.class);
        assertThatThrownBy(() -> page.edit(
                "hamlet-2026", "소개", "시놉시스", null,
                "비극", "12세 이상", 120, null, 0L,
                null, null, "#0F6F5D", "문의", "channel", "Bandi",
                null, null, null, PUBLISH_START,
                PUBLISH_START.minusSeconds(1)))
                .isInstanceOf(InvalidPerformancePublicPageException.class);
    }

    @Test
    void 보관한_공개_페이지는_수정하거나_다시_공개할_수_없다() {
        PerformancePublicPage page = draft("hamlet-2026")
                .changeStatus(PublicPageStatus.PUBLISHED)
                .changeStatus(PublicPageStatus.ENDED)
                .changeStatus(PublicPageStatus.ARCHIVED);

        assertThatThrownBy(() -> page.changeStatus(
                PublicPageStatus.PUBLISHED))
                .isInstanceOf(InvalidPerformancePublicPageException.class);
        assertThatThrownBy(() -> page.edit(
                "hamlet-2026", "소개", "시놉시스", null,
                "비극", "12세 이상", 120, null, 0L,
                null, null, "#0F6F5D", "문의", "channel", "Bandi",
                null, null, null, null, null))
                .isInstanceOf(InvalidPerformancePublicPageException.class);
    }

    private PerformancePublicPage draft(String slug) {
        return PerformancePublicPage.draft(
                1L, slug, "짧은 소개", "상세 시놉시스", "연출 의도",
                "비극", "12세 이상", 120, 15, 0L,
                10L, 11L, "#0F6F5D", "공연 문의", "bandi@example.com",
                "Bandi", "햄릿 2026", "공연 소개", 12L,
                null, null);
    }
}
