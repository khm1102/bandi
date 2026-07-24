package kr.ac.tukorea.bandi.domain.member.model;

import kr.ac.tukorea.bandi.domain.member.exception.InactiveCohortException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CohortTest {

    @Test
    void 활성_기수에는_멤버를_배정할_수_있다() {
        // given
        Cohort cohort = new Cohort(1L, "26-2기", true);

        // when & then
        assertThatCode(cohort::validateAssignable).doesNotThrowAnyException();
    }

    @Test
    void 비활성_기수에는_멤버를_배정할_수_없다() {
        // given
        Cohort cohort = new Cohort(1L, "26-2기", false);

        // when & then
        assertThatThrownBy(cohort::validateAssignable)
                .isInstanceOf(InactiveCohortException.class);
    }
}
