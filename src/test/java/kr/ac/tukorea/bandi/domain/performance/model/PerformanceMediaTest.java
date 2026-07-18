package kr.ac.tukorea.bandi.domain.performance.model;

import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPerformanceContentException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PerformanceMediaTest {

    @Test
    void 공연_미디어를_비공개_초안으로_생성한다() {
        PerformanceMedia media = PerformanceMedia.create(
                1L, 2L, MediaType.REHEARSAL, "연습실 사진",
                "1막 연습", "배우들이 무대에서 연습하는 모습",
                "촬영 김사진", null, 1);

        assertThat(media.isPublished()).isFalse();
        assertThat(media.getDisplayOrder()).isEqualTo(1);
    }

    @Test
    void 외부_URL은_HTTP_또는_HTTPS만_허용한다() {
        assertThatThrownBy(() -> PerformanceMedia.create(
                1L, 2L, MediaType.VIDEO, "트레일러",
                "공연 트레일러", "공연 트레일러 영상",
                "영상팀", "javascript:alert(1)", 0))
                .isInstanceOf(InvalidPerformanceContentException.class);
    }

    @Test
    void 제목과_대체_텍스트와_크레딧은_필수다() {
        assertThatThrownBy(() -> PerformanceMedia.create(
                1L, 2L, MediaType.POSTER, "포스터",
                "설명", "", "디자인팀", null, 0))
                .isInstanceOf(InvalidPerformanceContentException.class);
    }
}
