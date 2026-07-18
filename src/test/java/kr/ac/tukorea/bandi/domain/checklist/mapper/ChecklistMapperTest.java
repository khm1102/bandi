package kr.ac.tukorea.bandi.domain.checklist.mapper;

import kr.ac.tukorea.bandi.domain.checklist.dto.request.ChecklistItemSearchCondition;
import kr.ac.tukorea.bandi.domain.checklist.model.ChecklistItem;
import kr.ac.tukorea.bandi.domain.checklist.model.ChecklistItemHistory;
import kr.ac.tukorea.bandi.domain.checklist.model.ChecklistScope;
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
import kr.ac.tukorea.bandi.domain.performance.mapper.PerformanceRoundMapper;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceProject;
import kr.ac.tukorea.bandi.domain.performance.model.PerformanceRound;
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
class ChecklistMapperTest {

    private static final LocalDateTime NOW =
            LocalDateTime.of(2032, 6, 20, 17, 0);
    private static final LocalDateTime START =
            LocalDateTime.of(2032, 6, 21, 19, 0);

    private final ChecklistMapper checklistMapper;
    private final PerformanceProjectMapper projectMapper;
    private final PerformanceRoundMapper roundMapper;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;
    private final MemberMapper memberMapper;
    private final JdbcTemplate jdbcTemplate;

    private Long teamId;
    private Long adminId;
    private PerformanceProject project;
    private PerformanceRound round;

    @Autowired
    ChecklistMapperTest(ChecklistMapper checklistMapper,
                        PerformanceProjectMapper projectMapper,
                        PerformanceRoundMapper roundMapper,
                        TeamMapper teamMapper, CohortMapper cohortMapper,
                        MemberMapper memberMapper,
                        JdbcTemplate jdbcTemplate) {
        this.checklistMapper = checklistMapper;
        this.projectMapper = projectMapper;
        this.roundMapper = roundMapper;
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
        Cohort cohort = new Cohort(null, "32-체크", (short) 2032,
                CohortTerm.FIRST, true);
        cohortMapper.insert(cohort);
        Member admin = new Member(null, "2032000001", "체크운영진",
                null, null, null, teamId, cohort.getCohortId(),
                ClubRole.ADMIN, MemberStatus.ACTIVE,
                SsoLinkStatus.LINKED, null, null, null);
        memberMapper.insert(admin);
        adminId = admin.getMemberId();
        project = insertProject((short) 2032, "FIRST", "햄릿");
        round = insertRound(project);
    }

    @Test
    void 프로젝트와_회차_항목을_저장하고_범위순으로_조회한다() {
        ChecklistItem roundItem = ChecklistItem.round(
                project.getPerformanceProjectId(),
                round.getPerformanceRoundId(), teamId,
                "입장 동선 확인", true, 0, adminId);
        ChecklistItem projectItem = ChecklistItem.project(
                project.getPerformanceProjectId(), teamId,
                "무대 안전 확인", true, 0, adminId);
        checklistMapper.insert(roundItem);
        checklistMapper.insert(projectItem);

        assertThat(checklistMapper.search(condition(null, null)))
                .extracting("checklistItemId")
                .containsExactly(projectItem.getChecklistItemId(),
                        roundItem.getChecklistItemId());
        assertThat(checklistMapper.search(condition(
                round.getPerformanceRoundId(), ChecklistScope.ROUND)))
                .hasSize(1).first()
                .extracting("content")
                .isEqualTo("입장 동선 확인");
    }

    @Test
    void 완료_상태와_완료_취소_이력을_저장한다() {
        ChecklistItem item = insertProjectItem();
        ChecklistItem completed = item.changeCompleted(
                true, adminId, NOW);
        checklistMapper.update(completed);
        checklistMapper.insertHistory(ChecklistItemHistory.change(
                item.getChecklistItemId(), false, true,
                adminId, NOW, "현장 확인"));

        assertThat(checklistMapper.lookupByIdForUpdate(
                item.getChecklistItemId())).isPresent().get()
                .extracting(ChecklistItem::isCompleted)
                .isEqualTo(true);
        assertThat(checklistMapper.searchHistories(
                item.getChecklistItemId())).hasSize(1).first()
                .extracting("reason", "changedByMemberName")
                .containsExactly("현장 확인", "체크운영진");
    }

    @Test
    void 소프트_삭제한_항목은_조회와_잠금에서_제외한다() {
        ChecklistItem item = insertProjectItem();

        checklistMapper.delete(item.getChecklistItemId(), adminId, NOW);

        assertThat(checklistMapper.lookupByIdForUpdate(
                item.getChecklistItemId())).isEmpty();
        assertThat(checklistMapper.search(condition(null, null))).isEmpty();
    }

    @Test
    void 다른_프로젝트의_회차는_연결할_수_없다() {
        PerformanceProject other = insertProject(
                (short) 2032, "SECOND", "리어왕");

        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO checklist_item (
                    performance_project_id, performance_round_id,
                    team_id, scope_code, content, is_required,
                    display_order, is_completed,
                    created_by_member_id, updated_by_member_id
                ) VALUES (?, ?, ?, 'ROUND', '입장 확인', 1, 0, 0, ?, ?)
                """, other.getPerformanceProjectId(),
                round.getPerformanceRoundId(), teamId, adminId, adminId))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void DB는_범위와_완료_정보의_조합을_제약한다() {
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO checklist_item (
                    performance_project_id, performance_round_id,
                    team_id, scope_code, content, is_required,
                    display_order, is_completed,
                    created_by_member_id, updated_by_member_id
                ) VALUES (?, NULL, ?, 'ROUND', '오류', 1, 0, 0, ?, ?)
                """, project.getPerformanceProjectId(), teamId,
                adminId, adminId))
                .isInstanceOf(DataAccessException.class);
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO checklist_item (
                    performance_project_id, team_id, scope_code,
                    content, is_required, display_order, is_completed,
                    completed_by_member_id,
                    created_by_member_id, updated_by_member_id
                ) VALUES (?, ?, 'PROJECT', '오류', 1, 0, 1, ?, ?, ?)
                """, project.getPerformanceProjectId(), teamId,
                adminId, adminId, adminId))
                .isInstanceOf(DataAccessException.class);
    }

    private ChecklistItem insertProjectItem() {
        ChecklistItem item = ChecklistItem.project(
                project.getPerformanceProjectId(), teamId,
                "무대 안전 확인", true, 0, adminId);
        checklistMapper.insert(item);
        return item;
    }

    private ChecklistItemSearchCondition condition(
            Long performanceRoundId, ChecklistScope scope) {
        return new ChecklistItemSearchCondition(
                project.getPerformanceProjectId(),
                performanceRoundId, null, scope);
    }

    private PerformanceProject insertProject(short year, String term,
                                             String title) {
        PerformanceProject target = PerformanceProject.planning(
                year, term, title, LocalDate.of(year, 3, 1),
                LocalDate.of(year, 6, 30), "소극장", adminId);
        projectMapper.insert(target);
        return target;
    }

    private PerformanceRound insertRound(PerformanceProject targetProject) {
        PerformanceRound target = PerformanceRound.scheduled(
                targetProject.getPerformanceProjectId(), 1, START,
                START.minusMinutes(30), START.minusDays(20),
                START.minusDays(1));
        roundMapper.insertRound(target);
        return target;
    }
}
