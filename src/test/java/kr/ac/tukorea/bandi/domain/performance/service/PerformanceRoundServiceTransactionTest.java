package kr.ac.tukorea.bandi.domain.performance.service;

import kr.ac.tukorea.bandi.domain.member.mapper.CohortMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.MemberMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.TeamMapper;
import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.Cohort;
import kr.ac.tukorea.bandi.domain.member.model.CohortTerm;
import kr.ac.tukorea.bandi.domain.member.model.Member;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import kr.ac.tukorea.bandi.domain.member.model.SsoLinkStatus;
import kr.ac.tukorea.bandi.domain.member.service.MemberAccessContext;
import kr.ac.tukorea.bandi.domain.member.service.MemberService;
import kr.ac.tukorea.bandi.domain.performance.dto.request.PerformanceRoundWriteParam;
import kr.ac.tukorea.bandi.domain.performance.exception.DuplicatePerformanceContentException;
import kr.ac.tukorea.bandi.domain.performance.mapper.PerformanceProjectMapper;
import kr.ac.tukorea.bandi.domain.performance.mapper.PerformanceRoundMapper;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceProject;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceRound;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@ActiveProfiles("test")
class PerformanceRoundServiceTransactionTest {

    private static final LocalDateTime START =
            LocalDateTime.of(2996, 6, 21, 19, 0);

    private final PerformanceRoundService service;
    private final PerformanceRoundMapper roundMapper;
    private final PerformanceProjectMapper projectMapper;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;
    private final MemberMapper memberMapper;
    private final JdbcTemplate jdbcTemplate;

    @MockitoBean
    private MemberService memberService;
    @MockitoBean
    private PerformanceProjectService projectService;
    @MockitoBean
    private PerformancePublicPageService publicPageService;

    private Long cohortId;
    private Long adminId;
    private Long projectId;
    private Long secondRoundId;

    @Autowired
    PerformanceRoundServiceTransactionTest(
            PerformanceRoundService service,
            PerformanceRoundMapper roundMapper,
            PerformanceProjectMapper projectMapper,
            TeamMapper teamMapper, CohortMapper cohortMapper,
            MemberMapper memberMapper, JdbcTemplate jdbcTemplate) {
        this.service = service;
        this.roundMapper = roundMapper;
        this.projectMapper = projectMapper;
        this.teamMapper = teamMapper;
        this.cohortMapper = cohortMapper;
        this.memberMapper = memberMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void setUp() {
        Long teamId = teamMapper.searchAll().get(0).getTeamId();
        Cohort cohort = new Cohort(null, "회차트랜잭션", (short) 2996,
                CohortTerm.FIRST, true);
        cohortMapper.insert(cohort);
        cohortId = cohort.getCohortId();
        Member admin = new Member(null, "2996000001", "회차운영진",
                null, null, null, teamId, cohortId, ClubRole.ADMIN,
                MemberStatus.ACTIVE, SsoLinkStatus.LINKED,
                null, null, null);
        memberMapper.insert(admin);
        adminId = admin.getMemberId();
        given(memberService.lookupAccessContext(adminId))
                .willReturn(new MemberAccessContext(
                        adminId, teamId, true, false, true));
        PerformanceProject project = PerformanceProject.planning(
                (short) 2996, "FIRST", "회차 트랜잭션 공연",
                LocalDate.of(2996, 3, 1), LocalDate.of(2996, 6, 30),
                "소극장", adminId);
        projectMapper.insert(project);
        projectId = project.getPerformanceProjectId();
        insertRound(1, START);
        secondRoundId = insertRound(2, START.plusDays(1));
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM performance_round_accessibility");
        jdbcTemplate.update("DELETE FROM performance_round");
        jdbcTemplate.update("DELETE FROM performance_project");
        jdbcTemplate.update("DELETE FROM member WHERE member_id = ?", adminId);
        jdbcTemplate.update("DELETE FROM cohort WHERE cohort_id = ?", cohortId);
    }

    @Test
    void 중복_회차_번호_수정이_실패하면_기존_번호를_유지한다() {
        PerformanceRoundWriteParam duplicate =
                new PerformanceRoundWriteParam(secondRoundId,
                        projectId, 1, START.plusDays(1),
                        START.plusDays(1).minusMinutes(30),
                        START.minusDays(20), START.minusDays(1));

        assertThatThrownBy(() -> service.updateRound(adminId, duplicate))
                .isInstanceOf(DuplicatePerformanceContentException.class);

        assertThat(roundMapper.lookupRoundForUpdate(secondRoundId))
                .isPresent().get()
                .extracting(PerformanceRound::getRoundNo)
                .isEqualTo(2);
    }

    private Long insertRound(int roundNo, LocalDateTime startDttm) {
        PerformanceRound round = PerformanceRound.scheduled(
                projectId, roundNo, startDttm,
                startDttm.minusMinutes(30),
                startDttm.minusDays(20), startDttm.minusDays(1));
        roundMapper.insertRound(round);
        return round.getPerformanceRoundId();
    }
}
