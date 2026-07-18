package kr.ac.tukorea.bandi.domain.production.mapper;

import kr.ac.tukorea.bandi.domain.member.mapper.CohortMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.MemberMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.TeamMapper;
import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.Cohort;
import kr.ac.tukorea.bandi.domain.member.model.CohortTerm;
import kr.ac.tukorea.bandi.domain.member.model.Member;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import kr.ac.tukorea.bandi.domain.member.model.SsoLinkStatus;
import kr.ac.tukorea.bandi.domain.performance.mapper.PerformanceProjectMapper;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceProject;
import kr.ac.tukorea.bandi.domain.production.dto.request.ProductionTaskSearchCondition;
import kr.ac.tukorea.bandi.domain.production.dto.response.ProductionProgressResponse;
import kr.ac.tukorea.bandi.domain.production.dto.response.ProductionTaskResponse;
import kr.ac.tukorea.bandi.domain.production.model.ProductionTask;
import kr.ac.tukorea.bandi.domain.production.model.ProductionTaskHistory;
import kr.ac.tukorea.bandi.domain.production.model.ProductionTaskStatus;
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
class ProductionTaskMapperTest {

    private static final LocalDate CURRENT_DATE = LocalDate.of(2026, 3, 15);
    private static final LocalDate START_DATE = LocalDate.of(2026, 3, 1);
    private static final LocalDate DUE_DATE = LocalDate.of(2026, 3, 31);
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 3, 2, 12, 0);

    private final ProductionTaskMapper mapper;
    private final PerformanceProjectMapper performanceProjectMapper;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;
    private final MemberMapper memberMapper;
    private final JdbcTemplate jdbcTemplate;

    private Long firstTeamId;
    private Long secondTeamId;
    private Long adminId;
    private Long projectId;

    @Autowired
    ProductionTaskMapperTest(ProductionTaskMapper mapper,
                             PerformanceProjectMapper performanceProjectMapper,
                             TeamMapper teamMapper, CohortMapper cohortMapper,
                             MemberMapper memberMapper,
                             JdbcTemplate jdbcTemplate) {
        this.mapper = mapper;
        this.performanceProjectMapper = performanceProjectMapper;
        this.teamMapper = teamMapper;
        this.cohortMapper = cohortMapper;
        this.memberMapper = memberMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void setUp() {
        firstTeamId = teamId("무대팀");
        secondTeamId = teamId("오퍼팀");
        Cohort cohort = new Cohort(null, "26-제작", (short) 2026,
                CohortTerm.FIRST, true);
        cohortMapper.insert(cohort);
        Member admin = new Member(null, "2026000601", "제작 운영진",
                null, null, null, firstTeamId, cohort.getCohortId(),
                ClubRole.ADMIN, MemberStatus.ACTIVE,
                SsoLinkStatus.LINKED, null, null, null);
        memberMapper.insert(admin);
        adminId = admin.getMemberId();
        PerformanceProject project = PerformanceProject.planning(
                (short) 2028, "FIRST", "제작 테스트 공연",
                START_DATE, DUE_DATE.plusMonths(2), "대강당", adminId);
        performanceProjectMapper.insert(project);
        projectId = project.getPerformanceProjectId();
    }

    @Test
    void 제작_업무를_저장하고_수정하고_조건으로_조회한다() {
        ProductionTask task = insertTodo(firstTeamId, "무대 도면", DUE_DATE);
        mapper.update(task.edit("수정 도면", "수정 설명",
                START_DATE, DUE_DATE.plusDays(1), adminId));

        ProductionTaskResponse response = mapper.search(
                condition(firstTeamId, null, false), CURRENT_DATE).get(0);
        assertThat(response.title()).isEqualTo("수정 도면");
        assertThat(response.teamName()).isEqualTo("무대팀");
        assertThat(mapper.lookupByIdForUpdate(task.getProductionTaskId()))
                .isPresent();
    }

    @Test
    void 상태_변경과_이력을_저장하고_조회한다() {
        ProductionTask task = insertTodo(firstTeamId, "무대 도면", DUE_DATE);
        ProductionTask changed = task.changeStatus(
                ProductionTaskStatus.IN_PROGRESS, null, adminId);
        mapper.update(changed);
        mapper.insertHistory(ProductionTaskHistory.change(
                task.getProductionTaskId(), task.getStatus(),
                changed.getStatus(), "제작 시작", adminId, NOW));

        assertThat(mapper.searchHistories(task.getProductionTaskId()))
                .singleElement().satisfies(history -> {
                    assertThat(history.changedByName()).isEqualTo("제작 운영진");
                    assertThat(history.newStatus())
                            .isEqualTo(ProductionTaskStatus.IN_PROGRESS);
                });
    }

    @Test
    void 전체와_팀별_완료_차단_기한초과_진행률을_집계한다() {
        ProductionTask overdue = insertTodo(
                firstTeamId, "기한 초과", CURRENT_DATE.minusDays(1));
        ProductionTask completed = insertTodo(
                firstTeamId, "완료", CURRENT_DATE.minusDays(2));
        mapper.update(completed.changeStatus(
                ProductionTaskStatus.COMPLETED, null, adminId));
        ProductionTask blocked = insertTodo(
                secondTeamId, "차단", CURRENT_DATE.plusDays(1));
        mapper.update(blocked.changeStatus(
                ProductionTaskStatus.BLOCKED, "자재 대기", adminId));

        assertThat(mapper.lookupProjectProgress(projectId, CURRENT_DATE))
                .satisfies(progress -> {
                    assertThat(progress.totalCount()).isEqualTo(3);
                    assertThat(progress.completedCount()).isEqualTo(1);
                    assertThat(progress.blockedCount()).isEqualTo(1);
                    assertThat(progress.overdueCount()).isEqualTo(1);
                });
        assertThat(mapper.searchTeamProgress(projectId, CURRENT_DATE))
                .extracting(ProductionProgressResponse::teamName,
                        ProductionProgressResponse::totalCount)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("무대팀", 2),
                        org.assertj.core.groups.Tuple.tuple("오퍼팀", 1));
        assertThat(mapper.search(
                condition(null, null, true), CURRENT_DATE))
                .extracting(ProductionTaskResponse::productionTaskId)
                .containsExactly(overdue.getProductionTaskId());
    }

    @Test
    void 논리_삭제한_업무는_업무_이력_진행률에서_제외한다() {
        ProductionTask task = insertTodo(firstTeamId, "삭제 업무", DUE_DATE);
        mapper.insertHistory(ProductionTaskHistory.change(
                task.getProductionTaskId(), ProductionTaskStatus.TODO,
                ProductionTaskStatus.IN_PROGRESS, null, adminId, NOW));
        mapper.delete(task.getProductionTaskId(), adminId, NOW);

        assertThat(mapper.lookupByIdForUpdate(task.getProductionTaskId()))
                .isEmpty();
        assertThat(mapper.search(condition(null, null, false), CURRENT_DATE))
                .isEmpty();
        assertThat(mapper.searchHistories(task.getProductionTaskId())).isEmpty();
        assertThat(mapper.lookupProjectProgress(projectId, CURRENT_DATE)
                .totalCount()).isZero();
    }

    @Test
    void DB는_기간_상태와_BLOCKED_사유를_제약한다() {
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO production_task (
                    performance_project_id, team_id, title,
                    start_date, due_date, status_code, blocked_reason,
                    created_by_member_id, updated_by_member_id
                ) VALUES (?, ?, '기간 오류', ?, ?, 'TODO', NULL, ?, ?)
                """, projectId, firstTeamId, DUE_DATE, START_DATE,
                adminId, adminId))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO production_task (
                    performance_project_id, team_id, title,
                    status_code, blocked_reason,
                    created_by_member_id, updated_by_member_id
                ) VALUES (?, ?, '차단 오류', 'BLOCKED', NULL, ?, ?)
                """, projectId, firstTeamId, adminId, adminId))
                .isInstanceOf(DataAccessException.class);
    }

    private ProductionTask insertTodo(Long teamId, String title,
                                      LocalDate dueDate) {
        ProductionTask task = ProductionTask.todo(projectId, teamId,
                title, null, START_DATE, dueDate, adminId);
        mapper.insert(task);
        return task;
    }

    private ProductionTaskSearchCondition condition(
            Long teamId, ProductionTaskStatus status, boolean overdueOnly) {
        return new ProductionTaskSearchCondition(
                projectId, teamId, status, overdueOnly, 0, 20);
    }

    private Long teamId(String name) {
        return teamMapper.searchAll().stream()
                .filter(team -> team.getName().equals(name))
                .findFirst().orElseThrow().getTeamId();
    }
}
