package kr.ac.tukorea.bandi.domain.member.mapper;

import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.Cohort;
import kr.ac.tukorea.bandi.domain.member.model.CohortTerm;
import kr.ac.tukorea.bandi.domain.member.model.Member;
import kr.ac.tukorea.bandi.domain.member.model.MemberCohortHistory;
import kr.ac.tukorea.bandi.domain.member.model.MemberRoleHistory;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatusHistory;
import kr.ac.tukorea.bandi.domain.member.model.MemberTeamHistory;
import kr.ac.tukorea.bandi.domain.member.model.Team;
import kr.ac.tukorea.bandi.global.annotation.MapperTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@MapperTest
class MemberHistoryMapperTest {

    private static final LocalDateTime CHANGED_AT = LocalDateTime.of(2026, 7, 18, 10, 0);

    private final MemberHistoryMapper memberHistoryMapper;
    private final MemberMapper memberMapper;
    private final TeamMapper teamMapper;
    private final CohortMapper cohortMapper;

    private Long actorTeamId;
    private Long stageTeamId;
    private Long memberId;
    private Long adminId;
    private Long cohortId;
    private Long newCohortId;

    @Autowired
    MemberHistoryMapperTest(MemberHistoryMapper memberHistoryMapper, MemberMapper memberMapper,
                            TeamMapper teamMapper, CohortMapper cohortMapper) {
        this.memberHistoryMapper = memberHistoryMapper;
        this.memberMapper = memberMapper;
        this.teamMapper = teamMapper;
        this.cohortMapper = cohortMapper;
    }

    @BeforeEach
    void setUp() {
        List<Team> teams = teamMapper.searchAll();
        actorTeamId = teams.get(2).getTeamId();
        stageTeamId = teams.get(3).getTeamId();

        Cohort cohort = new Cohort(null, "26-2기", (short) 2026, CohortTerm.SECOND, true);
        cohortMapper.insert(cohort);
        cohortId = cohort.getCohortId();

        Cohort newCohort = new Cohort(null, "27-1기", (short) 2027, CohortTerm.FIRST, true);
        cohortMapper.insert(newCohort);
        newCohortId = newCohort.getCohortId();

        Member admin = Member.preRegister("2020184000", "이서준", actorTeamId, cohortId,
                ClubRole.ADMIN, null);
        memberMapper.insert(admin);
        adminId = admin.getMemberId();

        Member member = Member.preRegister("2021184000", "김하늘", actorTeamId, cohortId,
                ClubRole.MEMBER, adminId);
        memberMapper.insert(member);
        memberId = member.getMemberId();
    }

    @Test
    void 팀_변경_이력을_저장하고_멤버별로_조회한다() {
        // given
        MemberTeamHistory history = MemberTeamHistory.of(memberId, actorTeamId, stageTeamId,
                "팀 재배치", adminId, CHANGED_AT);

        // when
        memberHistoryMapper.insertTeamHistory(history);
        List<MemberTeamHistory> found = memberHistoryMapper.searchTeamHistoryByMemberId(memberId);

        // then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).previousTeamId()).isEqualTo(actorTeamId);
        assertThat(found.get(0).newTeamId()).isEqualTo(stageTeamId);
        assertThat(found.get(0).reason()).isEqualTo("팀 재배치");
        assertThat(found.get(0).changedByMemberId()).isEqualTo(adminId);
        assertThat(found.get(0).changedDttm()).isEqualTo(CHANGED_AT);
    }

    @Test
    void 권한_변경_이력을_저장하고_멤버별로_조회한다() {
        // given
        MemberRoleHistory history = MemberRoleHistory.of(memberId, ClubRole.MEMBER, ClubRole.LEADER,
                "무대팀장 선임", adminId, CHANGED_AT);

        // when
        memberHistoryMapper.insertRoleHistory(history);
        List<MemberRoleHistory> found = memberHistoryMapper.searchRoleHistoryByMemberId(memberId);

        // then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).previousRole()).isEqualTo(ClubRole.MEMBER);
        assertThat(found.get(0).newRole()).isEqualTo(ClubRole.LEADER);
        assertThat(found.get(0).reason()).isEqualTo("무대팀장 선임");
    }

    @Test
    void 기수_변경_이력을_저장하고_멤버별로_조회한다() {
        // given
        MemberCohortHistory history = MemberCohortHistory.of(memberId, cohortId, newCohortId,
                "기수 정정", adminId, CHANGED_AT);

        // when
        memberHistoryMapper.insertCohortHistory(history);
        List<MemberCohortHistory> found = memberHistoryMapper.searchCohortHistoryByMemberId(memberId);

        // then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).previousCohortId()).isEqualTo(cohortId);
        assertThat(found.get(0).newCohortId()).isEqualTo(newCohortId);
        assertThat(found.get(0).reason()).isEqualTo("기수 정정");
    }

    @Test
    void 상태_변경_이력을_저장하고_멤버별로_조회한다() {
        // given
        MemberStatusHistory history = MemberStatusHistory.of(memberId, MemberStatus.PRE_REGISTERED,
                MemberStatus.REGISTRATION_CANCELLED, "합격 취소", adminId, CHANGED_AT);

        // when
        memberHistoryMapper.insertStatusHistory(history);
        List<MemberStatusHistory> found = memberHistoryMapper.searchStatusHistoryByMemberId(memberId);

        // then
        assertThat(found).hasSize(1);
        assertThat(found.get(0).previousStatus()).isEqualTo(MemberStatus.PRE_REGISTERED);
        assertThat(found.get(0).newStatus()).isEqualTo(MemberStatus.REGISTRATION_CANCELLED);
        assertThat(found.get(0).reason()).isEqualTo("합격 취소");
    }

    @Test
    void 다른_멤버의_이력은_조회되지_않는다() {
        // given
        memberHistoryMapper.insertTeamHistory(MemberTeamHistory.of(memberId, actorTeamId, stageTeamId,
                "팀 재배치", adminId, CHANGED_AT));

        // when
        List<MemberTeamHistory> found = memberHistoryMapper.searchTeamHistoryByMemberId(adminId);

        // then
        assertThat(found).isEmpty();
    }
}
