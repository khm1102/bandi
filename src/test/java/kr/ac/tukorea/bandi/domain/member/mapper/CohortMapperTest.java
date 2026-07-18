package kr.ac.tukorea.bandi.domain.member.mapper;

import kr.ac.tukorea.bandi.domain.member.model.Cohort;
import kr.ac.tukorea.bandi.global.annotation.MapperTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@MapperTest
class CohortMapperTest {

    private final CohortMapper cohortMapper;

    @Autowired
    CohortMapperTest(CohortMapper cohortMapper) {
        this.cohortMapper = cohortMapper;
    }

    @Test
    void 기수를_저장하고_단건_조회한다() {
        // given
        Cohort cohort = new Cohort(null, "26-2기", (short) 2026, "SECOND", true);

        // when
        cohortMapper.insert(cohort);
        Optional<Cohort> found = cohortMapper.lookupById(cohort.getCohortId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("26-2기");
        assertThat(found.get().getAdmissionYear()).isEqualTo((short) 2026);
        assertThat(found.get().getTermCode()).isEqualTo("SECOND");
    }

    @Test
    void 같은_연도와_학기의_기수는_중복_저장할_수_없다() {
        // given — uk_cohort_year_term
        cohortMapper.insert(new Cohort(null, "26-2기", (short) 2026, "SECOND", true));

        // when & then — 표시명이 달라도 연도·학기 조합이 같으면 거부된다
        assertThatThrownBy(() -> cohortMapper.insert(
                new Cohort(null, "26-2기 추가모집", (short) 2026, "SECOND", true)))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void 같은_표시명의_기수는_중복_저장할_수_없다() {
        // given — uk_cohort_name
        cohortMapper.insert(new Cohort(null, "26-2기", (short) 2026, "SECOND", true));

        // when & then
        assertThatThrownBy(() -> cohortMapper.insert(
                new Cohort(null, "26-2기", (short) 2025, "FIRST", true)))
                .isInstanceOf(DuplicateKeyException.class);
    }
}
