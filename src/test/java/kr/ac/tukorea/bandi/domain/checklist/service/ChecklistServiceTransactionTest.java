package kr.ac.tukorea.bandi.domain.checklist.service;

import kr.ac.tukorea.bandi.domain.checklist.dto.request.ChecklistCompletionParam;
import kr.ac.tukorea.bandi.domain.checklist.mapper.ChecklistMapper;
import kr.ac.tukorea.bandi.domain.checklist.model.ChecklistItem;
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
import kr.ac.tukorea.bandi.domain.performance.service.PerformanceRoundService;
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
class ChecklistServiceTransactionTest {

    private final ChecklistService service;
    private final PerformanceProjectMapper projectMapper;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;
    private final MemberMapper memberMapper;
    private final JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private ChecklistMapper checklistMapper;
    @MockitoBean
    private MemberService memberService;
    @MockitoBean
    private PerformanceProjectService projectService;
    @MockitoBean
    private PerformanceRoundService roundService;
    @MockitoBean
    private Clock clock;

    private Long cohortId;
    private Long adminId;
    private Long projectId;
    private Long checklistItemId;

    @Autowired
    ChecklistServiceTransactionTest(
            ChecklistService service,
            PerformanceProjectMapper projectMapper,
            TeamMapper teamMapper, CohortMapper cohortMapper,
            MemberMapper memberMapper, JdbcTemplate jdbcTemplate) {
        this.service = service;
        this.projectMapper = projectMapper;
        this.teamMapper = teamMapper;
        this.cohortMapper = cohortMapper;
        this.memberMapper = memberMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    void setUp() {
        given(clock.instant()).willReturn(
                Instant.parse("2997-06-20T08:00:00Z"));
        given(clock.getZone()).willReturn(ZoneId.of("Asia/Seoul"));
        Long teamId = teamMapper.searchAll().get(0).getTeamId();
        Cohort cohort = new Cohort(null, "체크트랜잭션", (short) 2997,
                CohortTerm.FIRST, true);
        cohortMapper.insert(cohort);
        cohortId = cohort.getCohortId();
        Member admin = new Member(null, "2997000001", "체크운영진",
                null, null, null, teamId, cohortId, ClubRole.ADMIN,
                MemberStatus.ACTIVE, SsoLinkStatus.LINKED,
                null, null, null);
        memberMapper.insert(admin);
        adminId = admin.getMemberId();
        given(memberService.lookupAccessContext(adminId))
                .willReturn(new MemberAccessContext(
                        adminId, teamId, true, false, true));
        PerformanceProject project = PerformanceProject.planning(
                (short) 2997, "FIRST", "체크 트랜잭션 공연",
                LocalDate.of(2997, 3, 1), LocalDate.of(2997, 6, 30),
                "소극장", adminId);
        projectMapper.insert(project);
        projectId = project.getPerformanceProjectId();
        ChecklistItem item = ChecklistItem.project(projectId, teamId,
                "무대 안전 확인", true, 0, adminId);
        checklistMapper.insert(item);
        checklistItemId = item.getChecklistItemId();
    }

    @AfterEach
    void tearDown() {
        jdbcTemplate.update("DELETE FROM checklist_item_history");
        jdbcTemplate.update("DELETE FROM checklist_item");
        jdbcTemplate.update("DELETE FROM performance_project");
        jdbcTemplate.update("DELETE FROM member WHERE member_id = ?", adminId);
        jdbcTemplate.update("DELETE FROM cohort WHERE cohort_id = ?", cohortId);
    }

    @Test
    void 이력_저장이_실패하면_완료_상태_변경도_롤백한다() {
        willThrow(new IllegalStateException("이력 저장 실패"))
                .given(checklistMapper).insertHistory(any());

        assertThatThrownBy(() -> service.changeCompleted(adminId,
                new ChecklistCompletionParam(
                        checklistItemId, true, "현장 확인")))
                .isInstanceOf(IllegalStateException.class);

        assertThat(checklistMapper.lookupByIdForUpdate(checklistItemId))
                .isPresent().get()
                .extracting(ChecklistItem::isCompleted)
                .isEqualTo(false);
    }
}
