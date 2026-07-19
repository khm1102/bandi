package kr.ac.tukorea.bandi.domain.performance.model;

import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPerformanceViewingGuideException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PerformanceViewingGuideTest {

    @Test
    void 작품_전체의_관람_안내를_생성한다() {
        PerformanceViewingGuide guide = guide("공연 30분 전 입장");

        assertThat(guide.getPerformanceProjectId()).isEqualTo(1L);
        assertThat(guide.getParkingInformation()).isEqualTo("교내 주차장");
    }

    @Test
    void 핵심_관람_정책은_비어_있을_수_없다() {
        assertThatThrownBy(() -> guide(" "))
                .isInstanceOf(InvalidPerformanceViewingGuideException.class);
    }

    private PerformanceViewingGuide guide(String entryPolicy) {
        return PerformanceViewingGuide.create(1L, entryPolicy,
                "지연 입장은 안내에 따름", "촬영 및 녹음 금지",
                "공연 전날까지 취소", "휠체어 접근 가능",
                "정문에서 도보 5분", "교내 주차장");
    }
}
