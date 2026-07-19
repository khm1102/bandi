package kr.ac.tukorea.bandi.domain.member.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AcademicStatusTest {

    @Test
    void 학교_학적_라벨을_내부_코드로_정규화한다() {
        assertThat(AcademicStatus.fromPortalLabel("재학생")).isEqualTo(AcademicStatus.ENROLLED);
        assertThat(AcademicStatus.fromPortalLabel("휴학생")).isEqualTo(AcademicStatus.LEAVE_OF_ABSENCE);
        assertThat(AcademicStatus.fromPortalLabel("졸업생")).isEqualTo(AcademicStatus.GRADUATED);
    }

    @Test
    void 알_수_없는_학적은_UNKNOWN으로_정규화한다() {
        assertThat(AcademicStatus.fromPortalLabel("수료생")).isEqualTo(AcademicStatus.UNKNOWN);
        assertThat(AcademicStatus.fromPortalLabel(null)).isEqualTo(AcademicStatus.UNKNOWN);
        assertThat(AcademicStatus.fromPortalLabel(" ")).isEqualTo(AcademicStatus.UNKNOWN);
    }

    @Test
    void 재학생만_로그인을_허용한다() {
        assertThat(AcademicStatus.ENROLLED.isLoginAllowed()).isTrue();
        assertThat(AcademicStatus.LEAVE_OF_ABSENCE.isLoginAllowed()).isFalse();
        assertThat(AcademicStatus.GRADUATED.isLoginAllowed()).isFalse();
        assertThat(AcademicStatus.UNKNOWN.isLoginAllowed()).isFalse();
    }
}
