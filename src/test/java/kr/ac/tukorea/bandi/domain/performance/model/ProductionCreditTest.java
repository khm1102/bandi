package kr.ac.tukorea.bandi.domain.performance.model;

import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPerformanceContentException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductionCreditTest {

    @Test
    void 공개_프로필_없이도_제작진_크레딧을_생성한다() {
        ProductionCredit credit = ProductionCredit.create(
                1L, "연출", "김연출", null, 0);

        assertThat(credit.getPublicProfileId()).isNull();
        assertThat(credit.getCreditRole()).isEqualTo("연출");
    }

    @Test
    void 담당_분야와_공개_이름은_필수다() {
        assertThatThrownBy(() -> ProductionCredit.create(
                1L, "", "김연출", null, 0))
                .isInstanceOf(InvalidPerformanceContentException.class);
    }
}
