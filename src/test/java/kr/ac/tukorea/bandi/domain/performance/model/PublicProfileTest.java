package kr.ac.tukorea.bandi.domain.performance.model;

import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPublicProfileException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PublicProfileTest {

    @Test
    void 외부_참여자_공개_프로필을_초안으로_생성한다() {
        PublicProfile profile = PublicProfile.draft(
                null, "무대 위 이름", "소개", 3L,
                "https://example.com/profile");

        assertThat(profile.getMemberId()).isNull();
        assertThat(profile.getVisibilityStatus())
                .isEqualTo(PublicProfileVisibility.DRAFT);
    }

    @Test
    void 공개_이름은_필수이며_SNS는_HTTP_URL만_허용한다() {
        assertThatThrownBy(() -> PublicProfile.draft(
                1L, " ", null, null, null))
                .isInstanceOf(InvalidPublicProfileException.class);
        assertThatThrownBy(() -> PublicProfile.draft(
                1L, "배우", null, null, "javascript:alert(1)"))
                .isInstanceOf(InvalidPublicProfileException.class);
    }

    @Test
    void 초안_게시_보관_전이를_지원하고_보관한_프로필은_다시_게시하지_않는다() {
        PublicProfile profile = PublicProfile.draft(
                1L, "배우", null, null, null);

        PublicProfile published = profile.changeVisibility(
                PublicProfileVisibility.PUBLISHED);
        PublicProfile archived = published.changeVisibility(
                PublicProfileVisibility.ARCHIVED);

        assertThat(published.getVisibilityStatus())
                .isEqualTo(PublicProfileVisibility.PUBLISHED);
        assertThatThrownBy(() -> archived.changeVisibility(
                PublicProfileVisibility.PUBLISHED))
                .isInstanceOf(InvalidPublicProfileException.class);
    }
}
