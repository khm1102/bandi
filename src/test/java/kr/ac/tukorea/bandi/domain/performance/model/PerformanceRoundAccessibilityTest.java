package kr.ac.tukorea.bandi.domain.performance.model;

import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPerformanceContentException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PerformanceRoundAccessibilityTest {

    @Test
    void 음성_해설_지원을_생성한다() {
        PerformanceRoundAccessibility accessibility =
                PerformanceRoundAccessibility.create(
                        10L, AccessibilitySupportType.AUDIO_DESCRIPTION,
                        "음성 해설", "수신기는 안내 데스크에서 대여", 0);

        assertThat(accessibility.getSupportType())
                .isEqualTo(AccessibilitySupportType.AUDIO_DESCRIPTION);
        assertThat(accessibility.getTitle()).isEqualTo("음성 해설");
    }

    @Test
    void 지원_유형과_제목과_표시_순서를_검증한다() {
        assertThatThrownBy(() -> PerformanceRoundAccessibility.create(
                10L, null, "자막", null, 0))
                .isInstanceOf(InvalidPerformanceContentException.class);
        assertThatThrownBy(() -> PerformanceRoundAccessibility.create(
                10L, AccessibilitySupportType.CAPTION, " ", null, 0))
                .isInstanceOf(InvalidPerformanceContentException.class);
        assertThatThrownBy(() -> PerformanceRoundAccessibility.create(
                10L, AccessibilitySupportType.CAPTION, "자막", null, -1))
                .isInstanceOf(InvalidPerformanceContentException.class);
    }

    @Test
    void 지원_정보를_수정해도_회차는_변하지_않는다() {
        PerformanceRoundAccessibility accessibility =
                PerformanceRoundAccessibility.create(
                        10L, AccessibilitySupportType.CAPTION,
                        "한글 자막", null, 0);

        PerformanceRoundAccessibility changed = accessibility.edit(
                AccessibilitySupportType.SIGN_LANGUAGE,
                "수어 통역", "무대 우측", 1);

        assertThat(changed.getPerformanceRoundId()).isEqualTo(10L);
        assertThat(changed.getSupportType())
                .isEqualTo(AccessibilitySupportType.SIGN_LANGUAGE);
    }

    @Test
    void 다른_회차의_지원으로_취급할_수_없다() {
        PerformanceRoundAccessibility accessibility =
                PerformanceRoundAccessibility.create(
                        10L, AccessibilitySupportType.CAPTION,
                        "한글 자막", null, 0);

        assertThatThrownBy(() -> accessibility.validateRound(20L))
                .isInstanceOf(InvalidPerformanceContentException.class);
    }
}
