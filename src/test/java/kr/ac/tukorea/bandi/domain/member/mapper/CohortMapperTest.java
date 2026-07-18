package kr.ac.tukorea.bandi.domain.member.mapper;

import kr.ac.tukorea.bandi.domain.member.model.Cohort;
import kr.ac.tukorea.bandi.domain.member.model.CohortTerm;
import kr.ac.tukorea.bandi.global.annotation.MapperTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@MapperTest
class CohortMapperTest {

    private final CohortMapper cohortMapper;
    private final DataSource dataSource;

    @Autowired
    CohortMapperTest(CohortMapper cohortMapper, DataSource dataSource) {
        this.cohortMapper = cohortMapper;
        this.dataSource = dataSource;
    }

    @Test
    void 기수를_저장하고_단건_조회한다() {
        // given
        Cohort cohort = new Cohort(null, "26-2기", (short) 2026, CohortTerm.SECOND, true);

        // when
        cohortMapper.insert(cohort);
        Optional<Cohort> found = cohortMapper.lookupById(cohort.getCohortId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("26-2기");
        assertThat(found.get().getAdmissionYear()).isEqualTo((short) 2026);
        assertThat(found.get().getTermCode()).isEqualTo(CohortTerm.SECOND);
    }

    @Test
    void 같은_연도와_학기의_기수는_중복_저장할_수_없다() {
        // given — uk_cohort_year_term
        cohortMapper.insert(new Cohort(null, "26-2기", (short) 2026, CohortTerm.SECOND, true));

        // when & then — 표시명이 달라도 연도·학기 조합이 같으면 거부된다
        assertThatThrownBy(() -> cohortMapper.insert(
                new Cohort(null, "26-2기 추가모집", (short) 2026, CohortTerm.SECOND, true)))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void 같은_표시명의_기수는_중복_저장할_수_없다() {
        // given — uk_cohort_name
        cohortMapper.insert(new Cohort(null, "26-2기", (short) 2026, CohortTerm.SECOND, true));

        // when & then
        assertThatThrownBy(() -> cohortMapper.insert(
                new Cohort(null, "26-2기", (short) 2025, CohortTerm.FIRST, true)))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    void 허용되지_않는_term_code는_CHECK_제약으로_거부된다() throws SQLException {
        // given — Java enum을 우회한 직접 INSERT로 ck_cohort_term_code를 검증한다
        String sql = """
                INSERT INTO cohort (name, admission_year, term_code, is_active)
                VALUES ('오타 기수', 2099, 'SECNOD', 1)
                """;

        // when & then
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            assertThatThrownBy(() -> statement.executeUpdate(sql))
                    .isInstanceOf(SQLException.class)
                    .hasMessageContaining("ck_cohort_term_code");
        }
    }
}
