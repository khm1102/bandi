package kr.ac.tukorea.bandi.domain.production.service;

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
import kr.ac.tukorea.bandi.domain.performance.mapper.PerformanceProjectMapper;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceProject;
import kr.ac.tukorea.bandi.domain.performance.service.PerformanceProjectService;
import kr.ac.tukorea.bandi.domain.production.dto.request.ProductionTaskStatusParam;
import kr.ac.tukorea.bandi.domain.production.mapper.ProductionTaskMapper;
import kr.ac.tukorea.bandi.domain.production.model.ProductionTask;
import kr.ac.tukorea.bandi.domain.production.model.ProductionTaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

@SpringBootTest
@ActiveProfiles("test")
class ProductionTaskServiceTransactionTest {

    private static final LocalDate START_DATE = LocalDate.of(2026, 3, 1);
    private static final LocalDate DUE_DATE = LocalDate.of(2026, 3, 31);

    private final ProductionTaskService service;
    private final PerformanceProjectMapper performanceProjectMapper;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;
    private final MemberMapper memberMapper;
    private final JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private ProductionTaskMapper mapper;
    @MockitoBean
    private MemberService memberService;
    @MockitoBean
    private PerformanceProjectService performanceProjectService;
    @MockitoBean
    private Clock clock;

    private Long teamId;
    private Long cohortId;
    private Long adminId;
    private Long projectId;
    private Long taskId;

    @Autowired
    ProductionTaskServiceTransactionTest(
            ProductionTaskService service,
            PerformanceProjectMapper performanceProjectMapper,
            TeamMapper teamMapper, CohortMapper cohortMapper,
            MemberMapper memberMapper, JdbcTemplate jdbcTemplate) {
        this.service = service;
        this.performanceProjectMapper = performanceProjectMapper;
        this.teamMapper = teamMapper;
        this.cohortMapper = cohortMapper;
        this.memberMapper = memberMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void setUp() {
        given(clock.instant()).willReturn(Instant.parse("2026-03-15T03:00:00Z"));
        given(clock.getZone()).willReturn(ZoneId.of("Asia/Seoul"));
        teamId = teamMapper.searchAll().stream()
                .filter(team -> team.getName().equals("무대팀"))
                .findFirst().orElseThrow().getTeamId();
        Cohort cohort = new Cohort(null, "제작트랜잭션기수", (short) 2996,
                CohortTerm.FIRST, true);
        cohortMapper.insert(cohort);
        cohortId = cohort.getCohortId();
        Member admin = new Member(null, "2996000001", "제작운영진",
                null, null, null, teamId, cohortId, ClubRole.ADMIN,
                MemberStatus.ACTIVE, SsoLinkStatus.LINKED,
                null, null, null);
        memberMapper.insert(admin);
        adminId = admin.getMemberId();
        given(memberService.lookupAccessContext(adminId))
                .willReturn(new MemberAccessContext(
                        adminId, teamId, true, false, true));

        PerformanceProject project = PerformanceProject.planning(
                (short) 2996, "FIRST", "제작 트랜잭션 공연",
                START_DATE, DUE_DATE.plusMonths(2), "대강당", adminId);
        performanceProjectMapper.insert(project);
        projectId = project.getPerformanceProjectId();
        ProductionTask task = ProductionTask.todo(
                projectId, teamId, "상태 롤백", null,
                START_DATE, DUE_DATE, adminId);
        mapper.insert(task);
        taskId = task.getProductionTaskId();
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM production_task_history");
        jdbcTemplate.update("DELETE FROM production_task");
        jdbcTemplate.update("DELETE FROM performance_project");
        jdbcTemplate.update("DELETE FROM member WHERE member_id = ?", adminId);
        jdbcTemplate.update("DELETE FROM cohort WHERE cohort_id = ?", cohortId);
    }

    @Test
    void 이력_저장이_실패하면_업무_상태도_TODO로_롤백한다() {
        willThrow(new IllegalStateException("이력 저장 실패"))
                .given(mapper).insertHistory(any());

        assertThatThrownBy(() -> service.changeStatus(adminId,
                new ProductionTaskStatusParam(taskId,
                        ProductionTaskStatus.IN_PROGRESS, null, null)))
                .isInstanceOf(IllegalStateException.class);

        ProductionTask task = mapper.lookupByIdForUpdate(taskId).orElseThrow();
        assertThat(task.getStatus()).isEqualTo(ProductionTaskStatus.TODO);
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM production_task_history
                WHERE production_task_id = ?
                """, Integer.class, taskId);
        assertThat(count).isZero();
    }
}
