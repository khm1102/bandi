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
        Cohort cohort = new Cohort(null, "1-2", true);

        // when
        cohortMapper.insert(cohort);
        Optional<Cohort> found = cohortMapper.lookupById(cohort.getCohortId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("1-2");
    }

    @Test
    void 같은_표시명의_기수는_중복_저장할_수_없다() {
        // given — uk_cohort_name
        cohortMapper.insert(new Cohort(null, "1-2", true));

        // when & then
        assertThatThrownBy(() -> cohortMapper.insert(
                new Cohort(null, "1-2", true)))
                .isInstanceOf(DuplicateKeyException.class);
    }
}
