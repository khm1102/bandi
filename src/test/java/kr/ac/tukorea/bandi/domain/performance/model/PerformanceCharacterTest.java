package kr.ac.tukorea.bandi.domain.performance.model;

import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPerformanceContentException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PerformanceCharacterTest {

    @Test
    void 등장인물을_중요도와_표시_순서로_생성한다() {
        PerformanceCharacter character = PerformanceCharacter.create(
                1L, "햄릿", "덴마크의 왕자", CharacterImportance.LEAD, 1);

        assertThat(character.getImportance())
                .isEqualTo(CharacterImportance.LEAD);
        assertThat(character.getDisplayOrder()).isEqualTo(1);
    }

    @Test
    void 이름은_필수이고_표시_순서는_음수일_수_없다() {
        assertThatThrownBy(() -> PerformanceCharacter.create(
                1L, " ", null, CharacterImportance.LEAD, -1))
                .isInstanceOf(InvalidPerformanceContentException.class);
    }
}
