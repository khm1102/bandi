package kr.ac.tukorea.bandi.domain.member.service;

import kr.ac.tukorea.bandi.domain.member.dto.request.MemberSearchCondition;
import kr.ac.tukorea.bandi.domain.member.dto.response.CohortResponse;
import kr.ac.tukorea.bandi.domain.member.dto.response.MemberHistoryResponse;
import kr.ac.tukorea.bandi.domain.member.dto.response.MemberResponse;
import kr.ac.tukorea.bandi.domain.member.dto.response.TeamResponse;
import kr.ac.tukorea.bandi.domain.member.exception.MemberNotFoundException;
import kr.ac.tukorea.bandi.domain.member.mapper.CohortMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.MemberHistoryMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.MemberMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.TeamMapper;
import kr.ac.tukorea.bandi.domain.member.model.AcademicStatus;
import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.Cohort;
import kr.ac.tukorea.bandi.domain.member.model.CohortTerm;
import kr.ac.tukorea.bandi.domain.member.model.Member;
import kr.ac.tukorea.bandi.domain.member.model.MemberCohortHistory;
import kr.ac.tukorea.bandi.domain.member.model.MemberRoleHistory;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatusHistory;
import kr.ac.tukorea.bandi.domain.member.model.MemberTeamHistory;
import kr.ac.tukorea.bandi.domain.member.model.SsoLinkStatus;
import kr.ac.tukorea.bandi.domain.member.model.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberQueryServiceTest {

    private static final Long MEMBER_ID = 10L;
    private static final Long TEAM_ID = 20L;
    private static final Long COHORT_ID = 30L;
    private static final Long ACTOR_ID = 40L;
    private static final LocalDateTime CHANGED_AT =
            LocalDateTime.of(2026, 7, 19, 14, 0);

    @Mock
    private MemberMapper memberMapper;
    @Mock
    private TeamMapper teamMapper;
    @Mock
    private CohortMapper cohortMapper;
    @Mock
    private MemberHistoryMapper memberHistoryMapper;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberService(memberMapper, teamMapper,
                cohortMapper, memberHistoryMapper, Clock.systemUTC());
    }

    @Test
    void 조건에_맞는_멤버를_응답_DTO로_조회한다() {
        MemberSearchCondition condition = new MemberSearchCondition(
                "서준", TEAM_ID, MemberStatus.ACTIVE, ClubRole.MEMBER,
                SsoLinkStatus.LINKED);
        given(memberMapper.searchByCondition(condition))
                .willReturn(List.of(member()));

        List<MemberResponse> result = memberService.searchMembers(condition);

        assertThat(result).containsExactly(MemberResponse.from(member()));
        verify(memberMapper).searchByCondition(condition);
    }

    @Test
    void 멤버_상세를_조회한다() {
        given(memberMapper.lookupById(MEMBER_ID))
                .willReturn(Optional.of(member()));

        MemberResponse result = memberService.lookupMember(MEMBER_ID);

        assertThat(result.memberId()).isEqualTo(MEMBER_ID);
        assertThat(result.studentNo()).isEqualTo("2020184000");
        assertThat(result.academicStatus()).isEqualTo(AcademicStatus.ENROLLED);
        assertThat(result.ssoLinkStatus()).isEqualTo(SsoLinkStatus.LINKED);
    }

    @Test
    void 존재하지_않는_멤버_상세는_예외가_발생한다() {
        given(memberMapper.lookupById(MEMBER_ID)).willReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.lookupMember(MEMBER_ID))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    void 멤버의_팀_기수_권한_상태_이력을_함께_조회한다() {
        MemberTeamHistory teamHistory = new MemberTeamHistory(1L, MEMBER_ID,
                1L, TEAM_ID, "팀 변경", ACTOR_ID, CHANGED_AT);
        MemberCohortHistory cohortHistory = new MemberCohortHistory(2L,
                MEMBER_ID, 2L, COHORT_ID, "기수 변경", ACTOR_ID, CHANGED_AT);
        MemberRoleHistory roleHistory = new MemberRoleHistory(3L, MEMBER_ID,
                ClubRole.MEMBER, ClubRole.LEADER, "팀장 지정", ACTOR_ID,
                CHANGED_AT);
        MemberStatusHistory statusHistory = new MemberStatusHistory(4L,
                MEMBER_ID, MemberStatus.SUSPENDED, MemberStatus.ACTIVE,
                "복귀", ACTOR_ID, CHANGED_AT);
        given(memberMapper.lookupById(MEMBER_ID))
                .willReturn(Optional.of(member()));
        given(memberHistoryMapper.searchTeamHistoryByMemberId(MEMBER_ID))
                .willReturn(List.of(teamHistory));
        given(memberHistoryMapper.searchCohortHistoryByMemberId(MEMBER_ID))
                .willReturn(List.of(cohortHistory));
        given(memberHistoryMapper.searchRoleHistoryByMemberId(MEMBER_ID))
                .willReturn(List.of(roleHistory));
        given(memberHistoryMapper.searchStatusHistoryByMemberId(MEMBER_ID))
                .willReturn(List.of(statusHistory));

        MemberHistoryResponse result =
                memberService.lookupMemberHistory(MEMBER_ID);

        assertThat(result.teamHistories()).hasSize(1);
        assertThat(result.cohortHistories()).hasSize(1);
        assertThat(result.roleHistories()).hasSize(1);
        assertThat(result.statusHistories()).hasSize(1);
    }

    @Test
    void 활성_팀만_조회한다() {
        given(teamMapper.searchAll()).willReturn(List.of(
                new Team(1L, "배우", 1, true),
                new Team(2L, "종료 팀", 2, false)));

        List<TeamResponse> result = memberService.searchTeams(true);

        assertThat(result).extracting(TeamResponse::teamId)
                .containsExactly(1L);
    }

    @Test
    void 활성_기수만_조회한다() {
        given(cohortMapper.searchAll()).willReturn(List.of(
                new Cohort(1L, "26-2기", (short) 2026,
                        CohortTerm.SECOND, true),
                new Cohort(2L, "25-1기", (short) 2025,
                        CohortTerm.FIRST, false)));

        List<CohortResponse> result = memberService.searchCohorts(true);

        assertThat(result).extracting(CohortResponse::cohortId)
                .containsExactly(1L);
    }

    private Member member() {
        return new Member(MEMBER_ID, "2020184000", "이서준",
                "컴퓨터공학부", AcademicStatus.ENROLLED, CHANGED_AT,
                TEAM_ID, COHORT_ID, ClubRole.MEMBER, MemberStatus.ACTIVE,
                SsoLinkStatus.LINKED, CHANGED_AT, CHANGED_AT, ACTOR_ID);
    }
}
