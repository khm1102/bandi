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
import kr.ac.tukorea.bandi.domain.performance.model.AccessibilitySupportType;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceProject;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceRound;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceRoundAccessibility;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceRoundStatus;
import kr.ac.tukorea.bandi.global.annotation.MapperTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@MapperTest
class PerformanceRoundMapperTest {

    private static final LocalDateTime RESERVATION_OPEN =
            LocalDateTime.of(2026, 11, 1, 10, 0);
    private static final LocalDateTime RESERVATION_CLOSE =
            LocalDateTime.of(2026, 11, 20, 18, 0);
    private static final LocalDateTime ENTRY_START =
            LocalDateTime.of(2026, 11, 21, 18, 30);
    private static final LocalDateTime START =
            LocalDateTime.of(2026, 11, 21, 19, 0);

    private final PerformanceRoundMapper roundMapper;
    private final PerformanceProjectMapper projectMapper;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;
    private final MemberMapper memberMapper;
    private final JdbcTemplate jdbcTemplate;

    private Long adminId;
    private PerformanceProject project;

    @Autowired
    PerformanceRoundMapperTest(PerformanceRoundMapper roundMapper,
                               PerformanceProjectMapper projectMapper,
                               TeamMapper teamMapper,
                               CohortMapper cohortMapper,
                               MemberMapper memberMapper,
                               JdbcTemplate jdbcTemplate) {
        this.roundMapper = roundMapper;
        this.projectMapper = projectMapper;
        this.teamMapper = teamMapper;
        this.cohortMapper = cohortMapper;
        this.memberMapper = memberMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void setUp() {
        Long teamId = teamMapper.searchAll().stream()
                .filter(team -> team.getName().equals("연출"))
                .findFirst().orElseThrow().getTeamId();
        Cohort cohort = new Cohort(null, "26-회차", (short) 2026,
                CohortTerm.FIRST, true);
        cohortMapper.insert(cohort);
        Member admin = new Member(null, "2026000551", "회차 운영진",
                null, null, null, teamId, cohort.getCohortId(),
                ClubRole.ADMIN, MemberStatus.ACTIVE,
                SsoLinkStatus.LINKED, null, null, null);
        memberMapper.insert(admin);
        adminId = admin.getMemberId();
        project = insertProject((short) 2031, "FIRST", "햄릿");
    }

    @Test
    void 회차를_저장하고_수정하고_시간순으로_조회한다() {
        PerformanceRound second = insertRound(project, 2,
                START.plusDays(1));
        PerformanceRound first = insertRound(project, 1, START);

        roundMapper.updateRound(second.changeStatus(
                PerformanceRoundStatus.RESERVATION_OPEN));

        assertThat(roundMapper.lookupRoundForUpdate(
                second.getPerformanceRoundId())).isPresent().get()
                .extracting(PerformanceRound::getStatus)
                .isEqualTo(PerformanceRoundStatus.RESERVATION_OPEN);
        assertThat(roundMapper.searchRounds(
                project.getPerformanceProjectId()))
                .extracting("performanceRoundId")
                .containsExactly(first.getPerformanceRoundId(),
                        second.getPerformanceRoundId());
    }

    @Test
    void 같은_프로젝트의_회차_번호는_중복할_수_없다() {
        insertRound(project, 1, START);

        assertThatThrownBy(() -> insertRound(project, 1,
                START.plusDays(1)))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void 같은_프로젝트에서_시작_시각은_중복될_수_있다() {
        insertRound(project, 1, START);
        insertRound(project, 2, START);

        assertThat(roundMapper.searchRounds(
                project.getPerformanceProjectId())).hasSize(2);
    }

    @Test
    void 접근성_지원을_표시_순서대로_조회하고_수정_삭제한다() {
        PerformanceRound round = insertRound(project, 1, START);
        PerformanceRoundAccessibility second = insertAccessibility(
                round, AccessibilitySupportType.SIGN_LANGUAGE,
                "수어 통역", 2);
        PerformanceRoundAccessibility first = insertAccessibility(
                round, AccessibilitySupportType.CAPTION,
                "한글 자막", 1);

        roundMapper.updateAccessibility(second.edit(
                AccessibilitySupportType.AUDIO_DESCRIPTION,
                "음성 해설", "수신기 대여", 2));

        assertThat(roundMapper.searchAccessibilities(
                round.getPerformanceRoundId()))
                .extracting("performanceRoundAccessibilityId")
                .containsExactly(first.getPerformanceRoundAccessibilityId(),
                        second.getPerformanceRoundAccessibilityId());
        assertThat(roundMapper.searchAccessibilitiesByProject(
                project.getPerformanceProjectId())).hasSize(2);
        roundMapper.removeAccessibility(
                first.getPerformanceRoundAccessibilityId());
        assertThat(roundMapper.lookupAccessibilityForUpdate(
                first.getPerformanceRoundAccessibilityId())).isEmpty();
    }

    @Test
    void 같은_회차의_접근성_지원_유형은_중복할_수_없다() {
        PerformanceRound round = insertRound(project, 1, START);
        insertAccessibility(round, AccessibilitySupportType.CAPTION,
                "한글 자막", 0);

        assertThatThrownBy(() -> insertAccessibility(round,
                AccessibilitySupportType.CAPTION, "영문 자막", 1))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void DB는_회차_시각과_접근성_지원_유형을_제약한다() {
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO performance_round (
                    performance_project_id, round_no,
                    start_dttm, entry_start_dttm,
                    reservation_open_dttm, reservation_close_dttm,
                    status_code
                ) VALUES (?, 1, ?, ?, ?, ?, 'SCHEDULED')
                """, project.getPerformanceProjectId(), START,
                ENTRY_START, RESERVATION_CLOSE, RESERVATION_OPEN))
                .isInstanceOf(DataAccessException.class);
        PerformanceRound round = insertRound(project, 1, START);
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO performance_round_accessibility (
                    performance_round_id, support_type_code,
                    title, display_order
                ) VALUES (?, 'BRAILLE', '점자 안내', 0)
                """, round.getPerformanceRoundId()))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void 캐스팅_이력의_회차는_같은_프로젝트에_속해야_한다() {
        PerformanceRound round = insertRound(project, 1, START);
        PerformanceProject other = insertProject(
                (short) 2031, "SECOND", "리어왕");
        Long characterId = insertCharacter(other);
        Long profileId = insertPublicProfile();

        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO performance_cast_history (
                    performance_project_id, performance_round_id,
                    performance_character_id, new_public_profile_id,
                    new_cast_type_code, scope_code, action_code,
                    changed_by_member_id, changed_dttm
                ) VALUES (?, ?, ?, ?, 'PRIMARY', 'ROUND', 'ASSIGN', ?, NOW(6))
                """, other.getPerformanceProjectId(),
                round.getPerformanceRoundId(), characterId,
                profileId, adminId))
                .isInstanceOf(DataAccessException.class);
    }

    private PerformanceRound insertRound(
            PerformanceProject targetProject, int roundNo,
            LocalDateTime startDttm) {
        PerformanceRound round = PerformanceRound.scheduled(
                targetProject.getPerformanceProjectId(), roundNo,
                startDttm, startDttm.minusMinutes(30),
                RESERVATION_OPEN, RESERVATION_CLOSE);
        roundMapper.insertRound(round);
        return round;
    }

    private PerformanceRoundAccessibility insertAccessibility(
            PerformanceRound round, AccessibilitySupportType supportType,
            String title, int displayOrder) {
        PerformanceRoundAccessibility accessibility =
                PerformanceRoundAccessibility.create(
                        round.getPerformanceRoundId(), supportType,
                        title, null, displayOrder);
        roundMapper.insertAccessibility(accessibility);
        return accessibility;
    }

    private PerformanceProject insertProject(short year, String term,
                                             String title) {
        PerformanceProject target = PerformanceProject.planning(
                year, term, title, LocalDate.of(year, 3, 1),
                LocalDate.of(year, 12, 31), "소극장", adminId);
        projectMapper.insert(target);
        return target;
    }

    private Long insertCharacter(PerformanceProject targetProject) {
        jdbcTemplate.update("""
                INSERT INTO performance_character (
                    performance_project_id, name,
                    importance_code, display_order
                ) VALUES (?, '리어', 'LEAD', 0)
                """, targetProject.getPerformanceProjectId());
        return jdbcTemplate.queryForObject(
                "SELECT LAST_INSERT_ID()", Long.class);
    }

    private Long insertPublicProfile() {
        jdbcTemplate.update("""
                INSERT INTO public_profile (
                    public_name, visibility_status_code
                ) VALUES ('배우', 'DRAFT')
                """);
        return jdbcTemplate.queryForObject(
                "SELECT LAST_INSERT_ID()", Long.class);
    }
}
