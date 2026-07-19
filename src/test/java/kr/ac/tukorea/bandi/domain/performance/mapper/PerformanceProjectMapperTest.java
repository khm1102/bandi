package kr.ac.tukorea.bandi.domain.performance.mapper;

import kr.ac.tukorea.bandi.domain.member.mapper.CohortMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.MemberMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.TeamMapper;
import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.Cohort;
import kr.ac.tukorea.bandi.domain.member.model.CohortTerm;
import kr.ac.tukorea.bandi.domain.member.model.Member;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import kr.ac.tukorea.bandi.domain.member.model.SsoLinkStatus;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceProjectSearchCondition;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceProject;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceProjectStatus;
import kr.ac.tukorea.bandi.global.annotation.MapperTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@MapperTest
class PerformanceProjectMapperTest {

    private static final LocalDate START_DATE = LocalDate.of(2026, 3, 1);
    private static final LocalDate END_DATE = LocalDate.of(2026, 6, 30);

    private final PerformanceProjectMapper mapper;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;
    private final MemberMapper memberMapper;
    private final JdbcTemplate jdbcTemplate;

    private Long teamId;
    private Long adminId;

    @Autowired
    PerformanceProjectMapperTest(PerformanceProjectMapper mapper,
                                 TeamMapper teamMapper,
                                 CohortMapper cohortMapper,
                                 MemberMapper memberMapper,
                                 JdbcTemplate jdbcTemplate) {
        this.mapper = mapper;
        this.teamMapper = teamMapper;
        this.cohortMapper = cohortMapper;
        this.memberMapper = memberMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void setUp() {
        teamId = teamMapper.searchAll().stream()
                .filter(team -> team.getName().equals("연출"))
                .findFirst().orElseThrow().getTeamId();
        Cohort cohort = new Cohort(null, "26-공연", (short) 2026,
                CohortTerm.FIRST, true);
        cohortMapper.insert(cohort);
        Member admin = new Member(null, "2026000501", "공연 운영진",
                null, null, null, teamId, cohort.getCohortId(),
                ClubRole.ADMIN, MemberStatus.ACTIVE,
                SsoLinkStatus.LINKED, null, null, null);
        memberMapper.insert(admin);
        adminId = admin.getMemberId();
    }

    @Test
    void 프로젝트를_저장하고_현재와_목록으로_조회한다() {
        PerformanceProject project = insertPlanning((short) 2026, "FIRST");

        mapper.update(project.edit((short) 2026, "FIRST", "수정 공연",
                START_DATE, END_DATE, "소극장", adminId));

        assertThat(mapper.lookupByIdForUpdate(project.getPerformanceProjectId()))
                .isPresent().get()
                .extracting(PerformanceProject::getTitle)
                .isEqualTo("수정 공연");
        assertThat(mapper.lookupCurrent((short) 2026, "FIRST")).isPresent();
        assertThat(mapper.search(new PerformanceProjectSearchCondition(
                (short) 2026, "FIRST", null, 0, 20)))
                .hasSize(1);
    }

    @Test
    void 취소되지_않은_프로젝트는_학기당_하나만_저장한다() {
        insertPlanning((short) 2026, "FIRST");

        assertThatThrownBy(() -> insertPlanning((short) 2026, "FIRST"))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void 취소하면_현재_조회에서_빠지고_같은_학기에_대체_프로젝트를_만든다() {
        PerformanceProject project = insertPlanning((short) 2026, "FIRST");
        mapper.update(project.changeStatus(
                PerformanceProjectStatus.CANCELLED, adminId));

        assertThat(mapper.lookupCurrent((short) 2026, "FIRST")).isEmpty();
        PerformanceProject replacement = insertPlanning((short) 2026, "FIRST");
        assertThat(replacement.getPerformanceProjectId())
                .isNotEqualTo(project.getPerformanceProjectId());
    }

    @Test
    void 종료와_보관_프로젝트는_학기_점유를_유지한다() {
        PerformanceProject project = insertPlanning((short) 2026, "SECOND");
        PerformanceProject ended = advanceToEnded(project);
        mapper.update(ended.changeStatus(
                PerformanceProjectStatus.ARCHIVED, adminId));

        assertThatThrownBy(() -> insertPlanning((short) 2026, "SECOND"))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void 논리_삭제하면_조회와_학기_점유에서_제외한다() {
        PerformanceProject project = insertPlanning((short) 2027, "FIRST");
        jdbcTemplate.update("""
                UPDATE performance_project SET deleted_dttm = NOW(6)
                WHERE performance_project_id = ?
                """, project.getPerformanceProjectId());

        assertThat(mapper.lookupByIdForUpdate(project.getPerformanceProjectId()))
                .isEmpty();
        assertThat(mapper.lookupCurrent((short) 2027, "FIRST")).isEmpty();
        assertThat(mapper.search(new PerformanceProjectSearchCondition(
                (short) 2027, null, null, 0, 20))).isEmpty();
        assertThat(insertPlanning((short) 2027, "FIRST")
                .getPerformanceProjectId()).isNotNull();
    }

    @Test
    void DB는_제작_기간과_상태_코드를_제약한다() {
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO performance_project (
                    academic_year, term_code, title,
                    production_start_date, production_end_date,
                    place, status_code,
                    created_by_member_id, updated_by_member_id
                ) VALUES (2026, 'SUMMER-A', '오류 공연', ?, ?, '대강당',
                          'PLANNING', ?, ?)
                """, END_DATE, START_DATE, adminId, adminId))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO performance_project (
                    academic_year, term_code, title,
                    production_start_date, production_end_date,
                    place, status_code,
                    created_by_member_id, updated_by_member_id
                ) VALUES (2026, 'SUMMER-B', '오류 공연', ?, ?, '대강당',
                          'INVALID', ?, ?)
                """, START_DATE, END_DATE, adminId, adminId))
                .isInstanceOf(DataAccessException.class);
    }

    private PerformanceProject insertPlanning(short academicYear,
                                              String termCode) {
        PerformanceProject project = PerformanceProject.planning(
                academicYear, termCode,
                "%s %s 정기공연".formatted(academicYear, termCode),
                START_DATE, END_DATE, "대강당", adminId);
        mapper.insert(project);
        return project;
    }

    private PerformanceProject advanceToEnded(PerformanceProject project) {
        project = project.changeStatus(
                PerformanceProjectStatus.PRODUCING, adminId);
        project = project.changeStatus(
                PerformanceProjectStatus.RESERVATION_OPEN, adminId);
        project = project.changeStatus(
                PerformanceProjectStatus.PERFORMING, adminId);
        return project.changeStatus(PerformanceProjectStatus.ENDED, adminId);
    }
}
