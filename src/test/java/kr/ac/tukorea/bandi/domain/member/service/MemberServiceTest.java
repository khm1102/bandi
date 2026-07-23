package kr.ac.tukorea.bandi.domain.member.service;

import kr.ac.tukorea.bandi.domain.audit.model.AuditAction;
import kr.ac.tukorea.bandi.domain.audit.model.AuditTargetType;
import kr.ac.tukorea.bandi.domain.audit.service.AuditService;
import kr.ac.tukorea.bandi.domain.member.dto.request.CohortChangeParam;
import kr.ac.tukorea.bandi.domain.member.dto.request.MemberPreRegisterParam;
import kr.ac.tukorea.bandi.domain.member.dto.request.MemberPageSearchCondition;
import kr.ac.tukorea.bandi.domain.member.dto.request.MemberPageSearchParam;
import kr.ac.tukorea.bandi.domain.member.dto.request.MemberSearchCondition;
import kr.ac.tukorea.bandi.domain.member.dto.request.RoleChangeParam;
import kr.ac.tukorea.bandi.domain.member.dto.request.StatusChangeParam;
import kr.ac.tukorea.bandi.domain.member.dto.request.TeamChangeParam;
import kr.ac.tukorea.bandi.domain.member.exception.ChangeReasonRequiredException;
import kr.ac.tukorea.bandi.domain.member.exception.CohortNotFoundException;
import kr.ac.tukorea.bandi.domain.member.exception.DuplicateStudentNoException;
import kr.ac.tukorea.bandi.domain.member.exception.InactiveCohortException;
import kr.ac.tukorea.bandi.domain.member.exception.InactiveTeamException;
import kr.ac.tukorea.bandi.domain.member.exception.LastActiveAdminException;
import kr.ac.tukorea.bandi.domain.member.exception.MemberManagementForbiddenException;
import kr.ac.tukorea.bandi.domain.member.exception.MemberNotFoundException;
import kr.ac.tukorea.bandi.domain.member.exception.NoChangeException;
import kr.ac.tukorea.bandi.domain.member.exception.SelfRoleDemotionException;
import kr.ac.tukorea.bandi.domain.member.exception.TeamNotFoundException;
import kr.ac.tukorea.bandi.domain.member.mapper.CohortMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.ClubOfficerMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.MemberHistoryMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.MemberMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.TeamMapper;
import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.AcademicStatus;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");
    private static final Instant FIXED_INSTANT = Instant.parse("2026-07-18T01:00:00Z");
    private static final Long ACTOR_TEAM_ID = 3L;
    private static final Long STAGE_TEAM_ID = 4L;
    private static final Long COHORT_ID = 1L;
    private static final Long NEW_COHORT_ID = 2L;
    private static final Long ADMIN_ID = 100L;
    private static final Long TARGET_ID = 200L;
    private static final String REASON = "팀 재배치";

    @Mock
    private MemberMapper memberMapper;
    @Mock
    private TeamMapper teamMapper;
    @Mock
    private CohortMapper cohortMapper;
    @Mock
    private MemberHistoryMapper memberHistoryMapper;
    @Mock
    private ClubOfficerMapper clubOfficerMapper;
    @Mock
    private AuditService auditService;

    private MemberService memberService;

    @BeforeEach
    void setUp() {
        // 이력의 changed_dttm을 단언할 수 있도록 시각을 고정한다 (컨벤션 9.5).
        Clock clock = Clock.fixed(FIXED_INSTANT, SEOUL);
        memberService = new MemberService(memberMapper, teamMapper,
                cohortMapper, memberHistoryMapper, clubOfficerMapper,
                auditService, clock);
    }

    @Test
    void 활성_멤버_식별자를_전체_또는_팀으로_조회한다() {
        given(memberMapper.searchByCondition(any())).willReturn(List.of(
                member(ADMIN_ID, STAGE_TEAM_ID, ClubRole.ADMIN, MemberStatus.ACTIVE),
                member(TARGET_ID, STAGE_TEAM_ID, ClubRole.MEMBER, MemberStatus.ACTIVE)));

        List<Long> result = memberService.searchActiveMemberIds(STAGE_TEAM_ID);

        assertThat(result).containsExactly(ADMIN_ID, TARGET_ID);
        ArgumentCaptor<MemberSearchCondition> captor =
                ArgumentCaptor.forClass(MemberSearchCondition.class);
        verify(memberMapper).searchByCondition(captor.capture());
        assertThat(captor.getValue().teamId()).isEqualTo(STAGE_TEAM_ID);
        assertThat(captor.getValue().status()).isEqualTo(MemberStatus.ACTIVE);
    }

    @Test
    void 멤버_페이지는_목록과_같은_조건의_전체_건수로_메타데이터를_계산한다() {
        given(memberMapper.searchPage(any())).willReturn(List.of(
                member(TARGET_ID, STAGE_TEAM_ID, ClubRole.MEMBER, MemberStatus.ACTIVE)));
        given(memberMapper.countByPageCondition(any())).willReturn(41L);
        MemberPageSearchParam param = new MemberPageSearchParam("서준", STAGE_TEAM_ID,
                COHORT_ID, MemberStatus.ACTIVE, ClubRole.MEMBER,
                SsoLinkStatus.LINKED, 1, 20);

        var result = memberService.searchMemberPage(param);

        assertThat(result.items()).hasSize(1);
        assertThat(result.page()).isEqualTo(1);
        assertThat(result.totalElements()).isEqualTo(41);
        assertThat(result.totalPages()).isEqualTo(3);
        assertThat(result.hasPrevious()).isTrue();
        assertThat(result.hasNext()).isTrue();
        ArgumentCaptor<MemberPageSearchCondition> captor =
                ArgumentCaptor.forClass(MemberPageSearchCondition.class);
        verify(memberMapper).searchPage(captor.capture());
        verify(memberMapper).countByPageCondition(captor.getValue());
        assertThat(captor.getValue().offset()).isEqualTo(20);
    }

    @Test
    void 멤버_통계는_현재_페이지가_아닌_전체_활성_데이터로_계산한다() {
        given(memberMapper.countActive()).willReturn(42L);
        given(memberMapper.countSsoVerificationRequired()).willReturn(5L);
        given(cohortMapper.searchAll()).willReturn(List.of(cohort(),
                new Cohort(NEW_COHORT_ID, "27-1기", (short) 2027,
                        CohortTerm.FIRST, false)));

        var result = memberService.lookupMemberStats();

        assertThat(result.activeMemberCount()).isEqualTo(42);
        assertThat(result.activeCohortCount()).isEqualTo(1);
        assertThat(result.ssoVerificationRequiredCount()).isEqualTo(5);
    }

    private static Member member(Long memberId, Long teamId, ClubRole role, MemberStatus status) {
        return new Member(memberId, "2020184000", "이서준", "컴퓨터공학부", AcademicStatus.ENROLLED, null,
                teamId, COHORT_ID, role, status, SsoLinkStatus.LINKED, null, null, ADMIN_ID);
    }

    private static Team activeTeam(Long teamId) {
        return new Team(teamId, "무대팀", 4, true);
    }

    private static Cohort cohort() {
        return new Cohort(COHORT_ID, "26-2기", (short) 2026, CohortTerm.SECOND, true);
    }

    private void givenActiveAdmin() {
        given(memberMapper.lookupById(ADMIN_ID))
                .willReturn(Optional.of(member(ADMIN_ID, ACTOR_TEAM_ID, ClubRole.ADMIN, MemberStatus.ACTIVE)));
    }

    private void givenNonAdminActor() {
        given(memberMapper.lookupById(ADMIN_ID))
                .willReturn(Optional.of(member(ADMIN_ID, ACTOR_TEAM_ID, ClubRole.MEMBER, MemberStatus.ACTIVE)));
    }

    @Nested
    @DisplayName("사전 등록")
    class PreRegister {

        private MemberPreRegisterParam param() {
            return new MemberPreRegisterParam("2021184000", "김하늘", ACTOR_TEAM_ID, COHORT_ID);
        }

        @Test
        void 사전_등록하면_PRE_REGISTERED와_WAITING_상태로_저장된다() {
            // given
            given(memberMapper.existsByStudentNo("2021184000")).willReturn(false);
            given(teamMapper.lookupById(ACTOR_TEAM_ID)).willReturn(Optional.of(activeTeam(ACTOR_TEAM_ID)));
            given(cohortMapper.lookupById(COHORT_ID)).willReturn(Optional.of(cohort()));
            givenActiveAdmin();

            // when
            memberService.preRegister(ADMIN_ID, param());

            // then
            ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
            verify(memberMapper).insert(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(MemberStatus.PRE_REGISTERED);
            assertThat(captor.getValue().getSsoLinkStatus()).isEqualTo(SsoLinkStatus.WAITING);
            assertThat(captor.getValue().getRole()).isEqualTo(ClubRole.MEMBER);
        }

        @Test
        void 같은_학번은_중복_등록할_수_없다() {
            // given
            given(memberMapper.existsByStudentNo("2021184000")).willReturn(true);
            givenActiveAdmin();

            // when & then
            assertThatThrownBy(() -> memberService.preRegister(ADMIN_ID, param()))
                    .isInstanceOf(DuplicateStudentNoException.class);
            verify(memberMapper, never()).insert(any());
        }

        @Test
        void 동시_등록으로_DB_UNIQUE가_충돌해도_학번_중복_예외로_변환한다() {
            // given
            given(memberMapper.existsByStudentNo("2021184000")).willReturn(false);
            given(teamMapper.lookupById(ACTOR_TEAM_ID)).willReturn(Optional.of(activeTeam(ACTOR_TEAM_ID)));
            given(cohortMapper.lookupById(COHORT_ID)).willReturn(Optional.of(cohort()));
            given(memberMapper.insert(any())).willThrow(new DuplicateKeyException("uk_member_student_no"));
            givenActiveAdmin();

            // when & then
            assertThatThrownBy(() -> memberService.preRegister(ADMIN_ID, param()))
                    .isInstanceOf(DuplicateStudentNoException.class);
        }

        @Test
        void 존재하지_않는_팀으로는_등록할_수_없다() {
            // given
            given(memberMapper.existsByStudentNo("2021184000")).willReturn(false);
            given(teamMapper.lookupById(ACTOR_TEAM_ID)).willReturn(Optional.empty());
            givenActiveAdmin();

            // when & then
            assertThatThrownBy(() -> memberService.preRegister(ADMIN_ID, param()))
                    .isInstanceOf(TeamNotFoundException.class);
            verify(memberMapper, never()).insert(any());
        }

        @Test
        void 비활성_팀으로는_등록할_수_없다() {
            // given
            given(memberMapper.existsByStudentNo("2021184000")).willReturn(false);
            given(teamMapper.lookupById(ACTOR_TEAM_ID)).willReturn(Optional.of(new Team(ACTOR_TEAM_ID, "배우", 3, false)));
            givenActiveAdmin();

            // when & then
            assertThatThrownBy(() -> memberService.preRegister(ADMIN_ID, param()))
                    .isInstanceOf(InactiveTeamException.class);
            verify(memberMapper, never()).insert(any());
        }

        @Test
        void 존재하지_않는_기수로는_등록할_수_없다() {
            // given
            given(memberMapper.existsByStudentNo("2021184000")).willReturn(false);
            given(teamMapper.lookupById(ACTOR_TEAM_ID)).willReturn(Optional.of(activeTeam(ACTOR_TEAM_ID)));
            given(cohortMapper.lookupById(COHORT_ID)).willReturn(Optional.empty());
            givenActiveAdmin();

            // when & then
            assertThatThrownBy(() -> memberService.preRegister(ADMIN_ID, param()))
                    .isInstanceOf(CohortNotFoundException.class);
            verify(memberMapper, never()).insert(any());
        }

        @Test
        void 비활성_기수로는_등록할_수_없다() {
            // given
            givenActiveAdmin();
            given(memberMapper.existsByStudentNo("2021184000")).willReturn(false);
            given(teamMapper.lookupById(ACTOR_TEAM_ID)).willReturn(Optional.of(activeTeam(ACTOR_TEAM_ID)));
            given(cohortMapper.lookupById(COHORT_ID))
                    .willReturn(Optional.of(new Cohort(COHORT_ID, "26-2기", (short) 2026,
                            CohortTerm.SECOND, false)));

            // when & then
            assertThatThrownBy(() -> memberService.preRegister(ADMIN_ID, param()))
                    .isInstanceOf(InactiveCohortException.class);
            verify(memberMapper, never()).insert(any());
        }

        @Test
        void 활성_ADMIN이_아니면_멤버를_등록할_수_없다() {
            // given
            given(memberMapper.lookupById(ADMIN_ID))
                    .willReturn(Optional.of(member(ADMIN_ID, ACTOR_TEAM_ID,
                            ClubRole.MEMBER, MemberStatus.ACTIVE)));

            // when & then
            assertThatThrownBy(() -> memberService.preRegister(ADMIN_ID, param()))
                    .isInstanceOf(MemberManagementForbiddenException.class);
            verify(memberMapper, never()).insert(any());
        }
    }

    @Nested
    @DisplayName("팀 변경")
    class ChangeTeam {

        @Test
        void 팀을_변경하면_현재_팀과_변경_이력이_함께_저장된다() {
            // given
            given(memberMapper.lookupByIdForUpdate(TARGET_ID))
                    .willReturn(Optional.of(member(TARGET_ID, ACTOR_TEAM_ID, ClubRole.MEMBER, MemberStatus.ACTIVE)));
            given(memberMapper.lookupByIdForUpdate(ADMIN_ID))
                    .willReturn(Optional.of(member(ADMIN_ID, ACTOR_TEAM_ID, ClubRole.ADMIN, MemberStatus.ACTIVE)));
            given(teamMapper.lookupById(STAGE_TEAM_ID)).willReturn(Optional.of(activeTeam(STAGE_TEAM_ID)));

            // when
            memberService.changeTeam(ADMIN_ID, new TeamChangeParam(TARGET_ID, STAGE_TEAM_ID, REASON));

            // then
            verify(memberMapper).updateTeam(TARGET_ID, STAGE_TEAM_ID);
            ArgumentCaptor<MemberTeamHistory> captor = ArgumentCaptor.forClass(MemberTeamHistory.class);
            verify(memberHistoryMapper).insertTeamHistory(captor.capture());
            MemberTeamHistory history = captor.getValue();
            assertThat(history.previousTeamId()).isEqualTo(ACTOR_TEAM_ID);
            assertThat(history.newTeamId()).isEqualTo(STAGE_TEAM_ID);
            assertThat(history.reason()).isEqualTo(REASON);
            assertThat(history.changedByMemberId()).isEqualTo(ADMIN_ID);
            assertThat(history.changedDttm()).isEqualTo(LocalDateTime.ofInstant(FIXED_INSTANT, SEOUL));
            verify(auditService).record(ADMIN_ID,
                    AuditAction.MEMBER_TEAM_CHANGED,
                    AuditTargetType.MEMBER, TARGET_ID, "멤버 팀 변경");
        }

        @Test
        void 동일한_팀으로_변경하면_예외가_발생하고_아무것도_저장하지_않는다() {
            // given
            given(memberMapper.lookupByIdForUpdate(TARGET_ID))
                    .willReturn(Optional.of(member(TARGET_ID, ACTOR_TEAM_ID, ClubRole.MEMBER, MemberStatus.ACTIVE)));
            given(memberMapper.lookupByIdForUpdate(ADMIN_ID))
                    .willReturn(Optional.of(member(ADMIN_ID, ACTOR_TEAM_ID, ClubRole.ADMIN, MemberStatus.ACTIVE)));

            // when & then
            assertThatThrownBy(() -> memberService.changeTeam(ADMIN_ID,
                    new TeamChangeParam(TARGET_ID, ACTOR_TEAM_ID, REASON)))
                    .isInstanceOf(NoChangeException.class);
            verify(memberHistoryMapper, never()).insertTeamHistory(any());
        }

        @Test
        void 변경_사유가_비어_있으면_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> memberService.changeTeam(ADMIN_ID,
                    new TeamChangeParam(TARGET_ID, STAGE_TEAM_ID, "  ")))
                    .isInstanceOf(ChangeReasonRequiredException.class);
            verify(memberHistoryMapper, never()).insertTeamHistory(any());
        }

        @Test
        void 존재하지_않는_멤버의_팀은_변경할_수_없다() {
            // given
            given(memberMapper.lookupByIdForUpdate(TARGET_ID)).willReturn(Optional.empty());
            given(memberMapper.lookupByIdForUpdate(ADMIN_ID))
                    .willReturn(Optional.of(member(ADMIN_ID, ACTOR_TEAM_ID, ClubRole.ADMIN, MemberStatus.ACTIVE)));

            // when & then
            assertThatThrownBy(() -> memberService.changeTeam(ADMIN_ID,
                    new TeamChangeParam(TARGET_ID, STAGE_TEAM_ID, REASON)))
                    .isInstanceOf(MemberNotFoundException.class);
        }

        @Test
        void 활성_멤버는_본인_팀을_변경할_수_있다() {
            Member self = member(TARGET_ID, ACTOR_TEAM_ID, ClubRole.MEMBER,
                    MemberStatus.ACTIVE);
            given(memberMapper.lookupByIdForUpdate(TARGET_ID)).willReturn(Optional.of(self));
            given(teamMapper.lookupById(STAGE_TEAM_ID)).willReturn(Optional.of(activeTeam(STAGE_TEAM_ID)));

            memberService.changeTeam(TARGET_ID,
                    new TeamChangeParam(TARGET_ID, STAGE_TEAM_ID, REASON));

            verify(memberMapper).updateTeam(TARGET_ID, STAGE_TEAM_ID);
        }

        @Test
        void 활성_팀장은_현재_팀의_멤버만_변경할_수_있다() {
            Member leader = member(ADMIN_ID, ACTOR_TEAM_ID, ClubRole.LEADER,
                    MemberStatus.ACTIVE);
            given(memberMapper.lookupByIdForUpdate(ADMIN_ID)).willReturn(Optional.of(leader));
            given(memberMapper.lookupByIdForUpdate(TARGET_ID)).willReturn(Optional.of(
                    member(TARGET_ID, ACTOR_TEAM_ID, ClubRole.MEMBER, MemberStatus.ACTIVE)));
            given(teamMapper.lookupById(STAGE_TEAM_ID)).willReturn(Optional.of(activeTeam(STAGE_TEAM_ID)));

            memberService.changeTeam(ADMIN_ID,
                    new TeamChangeParam(TARGET_ID, STAGE_TEAM_ID, REASON));

            verify(memberMapper).updateTeam(TARGET_ID, STAGE_TEAM_ID);
        }

        @Test
        void 다른_팀_팀장은_멤버_팀을_변경할_수_없다() {
            Member leader = member(ADMIN_ID, STAGE_TEAM_ID, ClubRole.LEADER,
                    MemberStatus.ACTIVE);
            given(memberMapper.lookupByIdForUpdate(ADMIN_ID)).willReturn(Optional.of(leader));
            given(memberMapper.lookupByIdForUpdate(TARGET_ID)).willReturn(Optional.of(
                    member(TARGET_ID, ACTOR_TEAM_ID, ClubRole.MEMBER, MemberStatus.ACTIVE)));

            assertThatThrownBy(() -> memberService.changeTeam(ADMIN_ID,
                    new TeamChangeParam(TARGET_ID, STAGE_TEAM_ID, REASON)))
                    .isInstanceOf(MemberManagementForbiddenException.class);
            verify(memberMapper, never()).updateTeam(any(), any());
        }

        @Test
        void 일반_멤버는_다른_멤버의_팀을_변경할_수_없다() {
            given(memberMapper.lookupByIdForUpdate(ADMIN_ID)).willReturn(Optional.of(
                    member(ADMIN_ID, ACTOR_TEAM_ID, ClubRole.MEMBER, MemberStatus.ACTIVE)));
            given(memberMapper.lookupByIdForUpdate(TARGET_ID)).willReturn(Optional.of(
                    member(TARGET_ID, ACTOR_TEAM_ID, ClubRole.MEMBER, MemberStatus.ACTIVE)));

            assertThatThrownBy(() -> memberService.changeTeam(ADMIN_ID,
                    new TeamChangeParam(TARGET_ID, STAGE_TEAM_ID, REASON)))
                    .isInstanceOf(MemberManagementForbiddenException.class);
        }
    }

    @Nested
    @DisplayName("기수 변경")
    class ChangeCohort {

        @Test
        void 기수를_변경하면_현재_기수와_변경_이력이_함께_저장된다() {
            // given
            givenActiveAdmin();
            given(memberMapper.lookupByIdForUpdate(TARGET_ID))
                    .willReturn(Optional.of(member(TARGET_ID, ACTOR_TEAM_ID, ClubRole.MEMBER, MemberStatus.ACTIVE)));
            given(cohortMapper.lookupById(NEW_COHORT_ID))
                    .willReturn(Optional.of(new Cohort(NEW_COHORT_ID, "27-1기", (short) 2027,
                            CohortTerm.FIRST, true)));

            // when
            memberService.changeCohort(ADMIN_ID,
                    new CohortChangeParam(TARGET_ID, NEW_COHORT_ID, REASON));

            // then
            verify(memberMapper).updateCohort(TARGET_ID, NEW_COHORT_ID);
            ArgumentCaptor<MemberCohortHistory> captor = ArgumentCaptor.forClass(MemberCohortHistory.class);
            verify(memberHistoryMapper).insertCohortHistory(captor.capture());
            assertThat(captor.getValue().previousCohortId()).isEqualTo(COHORT_ID);
            assertThat(captor.getValue().newCohortId()).isEqualTo(NEW_COHORT_ID);
            assertThat(captor.getValue().changedByMemberId()).isEqualTo(ADMIN_ID);
            verify(auditService).record(ADMIN_ID,
                    AuditAction.MEMBER_COHORT_CHANGED,
                    AuditTargetType.MEMBER, TARGET_ID, "멤버 기수 변경");
        }

        @Test
        void 비활성_기수로는_변경할_수_없다() {
            // given
            givenActiveAdmin();
            given(memberMapper.lookupByIdForUpdate(TARGET_ID))
                    .willReturn(Optional.of(member(TARGET_ID, ACTOR_TEAM_ID, ClubRole.MEMBER, MemberStatus.ACTIVE)));
            given(cohortMapper.lookupById(NEW_COHORT_ID))
                    .willReturn(Optional.of(new Cohort(NEW_COHORT_ID, "27-1기", (short) 2027,
                            CohortTerm.FIRST, false)));

            // when & then
            assertThatThrownBy(() -> memberService.changeCohort(ADMIN_ID,
                    new CohortChangeParam(TARGET_ID, NEW_COHORT_ID, REASON)))
                    .isInstanceOf(InactiveCohortException.class);
            verify(memberMapper, never()).updateCohort(any(), any());
        }
    }

    @Nested
    @DisplayName("권한 변경")
    class ChangeRole {

        @Test
        void 역할을_변경하면_현재_역할과_변경_이력이_함께_저장된다() {
            // given
            given(memberMapper.lookupByIdForUpdate(TARGET_ID))
                    .willReturn(Optional.of(member(TARGET_ID, ACTOR_TEAM_ID, ClubRole.MEMBER, MemberStatus.ACTIVE)));
            givenActiveAdmin();

            // when
            memberService.changeRole(ADMIN_ID, new RoleChangeParam(TARGET_ID, ClubRole.LEADER, REASON));

            // then
            verify(memberMapper).updateRole(TARGET_ID, ClubRole.LEADER);
            ArgumentCaptor<MemberRoleHistory> captor = ArgumentCaptor.forClass(MemberRoleHistory.class);
            verify(memberHistoryMapper).insertRoleHistory(captor.capture());
            assertThat(captor.getValue().previousRole()).isEqualTo(ClubRole.MEMBER);
            assertThat(captor.getValue().newRole()).isEqualTo(ClubRole.LEADER);
            verify(auditService).record(ADMIN_ID,
                    AuditAction.MEMBER_ROLE_CHANGED,
                    AuditTargetType.MEMBER, TARGET_ID, "멤버 권한 변경");
        }

        @Test
        void ADMIN은_본인의_권한을_낮출_수_없다() {
            // given
            given(memberMapper.lookupById(TARGET_ID))
                    .willReturn(Optional.of(member(TARGET_ID, ACTOR_TEAM_ID, ClubRole.ADMIN, MemberStatus.ACTIVE)));
            given(memberMapper.lookupByIdForUpdate(TARGET_ID))
                    .willReturn(Optional.of(member(TARGET_ID, ACTOR_TEAM_ID, ClubRole.ADMIN, MemberStatus.ACTIVE)));

            // when & then
            assertThatThrownBy(() -> memberService.changeRole(TARGET_ID,
                    new RoleChangeParam(TARGET_ID, ClubRole.MEMBER, REASON)))
                    .isInstanceOf(SelfRoleDemotionException.class);
            verify(memberMapper, never()).updateRole(any(), any());
        }

        @Test
        void 다른_활성_ADMIN이_남아_있으면_ADMIN_권한을_낮출_수_있다() {
            // given
            given(memberMapper.lookupByIdForUpdate(TARGET_ID))
                    .willReturn(Optional.of(member(TARGET_ID, ACTOR_TEAM_ID, ClubRole.ADMIN, MemberStatus.ACTIVE)));
            given(memberMapper.searchActiveAdminIdsForUpdate()).willReturn(List.of(TARGET_ID, ADMIN_ID));
            givenActiveAdmin();

            // when
            memberService.changeRole(ADMIN_ID, new RoleChangeParam(TARGET_ID, ClubRole.MEMBER, REASON));

            // then
            verify(memberMapper).updateRole(TARGET_ID, ClubRole.MEMBER);
        }
    }

    @Nested
    @DisplayName("상태 변경")
    class ChangeStatus {

        @Test
        void 마지막_활성_ADMIN은_활동_중지할_수_없다() {
            // given
            given(memberMapper.lookupById(TARGET_ID))
                    .willReturn(Optional.of(member(TARGET_ID, ACTOR_TEAM_ID, ClubRole.ADMIN, MemberStatus.ACTIVE)));
            given(memberMapper.lookupByIdForUpdate(TARGET_ID))
                    .willReturn(Optional.of(member(TARGET_ID, ACTOR_TEAM_ID, ClubRole.ADMIN, MemberStatus.ACTIVE)));
            given(memberMapper.searchActiveAdminIdsForUpdate()).willReturn(List.of(TARGET_ID));

            // when & then
            assertThatThrownBy(() -> memberService.changeStatus(TARGET_ID,
                    new StatusChangeParam(TARGET_ID, MemberStatus.SUSPENDED, REASON)))
                    .isInstanceOf(LastActiveAdminException.class);
            verify(memberMapper, never()).updateStatus(any(), any());
        }

        @Test
        void 마지막_활성_ADMIN은_탈퇴_처리할_수_없다() {
            // given
            given(memberMapper.lookupById(TARGET_ID))
                    .willReturn(Optional.of(member(TARGET_ID, ACTOR_TEAM_ID, ClubRole.ADMIN, MemberStatus.ACTIVE)));
            given(memberMapper.lookupByIdForUpdate(TARGET_ID))
                    .willReturn(Optional.of(member(TARGET_ID, ACTOR_TEAM_ID, ClubRole.ADMIN, MemberStatus.ACTIVE)));
            given(memberMapper.searchActiveAdminIdsForUpdate()).willReturn(List.of(TARGET_ID));

            // when & then
            assertThatThrownBy(() -> memberService.changeStatus(TARGET_ID,
                    new StatusChangeParam(TARGET_ID, MemberStatus.WITHDRAWN, REASON)))
                    .isInstanceOf(LastActiveAdminException.class);
        }

        @Test
        void 사전_등록_멤버를_등록_취소_상태로_전환할_수_있다() {
            // given
            given(memberMapper.lookupByIdForUpdate(TARGET_ID)).willReturn(
                    Optional.of(member(TARGET_ID, ACTOR_TEAM_ID, ClubRole.MEMBER, MemberStatus.PRE_REGISTERED)));
            givenActiveAdmin();

            // when
            memberService.changeStatus(ADMIN_ID,
                    new StatusChangeParam(TARGET_ID, MemberStatus.REGISTRATION_CANCELLED, REASON));

            // then
            verify(memberMapper).updateStatus(TARGET_ID, MemberStatus.REGISTRATION_CANCELLED);
            ArgumentCaptor<MemberStatusHistory> captor = ArgumentCaptor.forClass(MemberStatusHistory.class);
            verify(memberHistoryMapper).insertStatusHistory(captor.capture());
            assertThat(captor.getValue().previousStatus()).isEqualTo(MemberStatus.PRE_REGISTERED);
            assertThat(captor.getValue().newStatus()).isEqualTo(MemberStatus.REGISTRATION_CANCELLED);
            assertThat(captor.getValue().reason()).isEqualTo(REASON);
            assertThat(captor.getValue().changedByMemberId()).isEqualTo(ADMIN_ID);
            verify(auditService).record(ADMIN_ID,
                    AuditAction.MEMBER_STATUS_CHANGED,
                    AuditTargetType.MEMBER, TARGET_ID, "멤버 상태 변경");
        }

        @Test
        void 같은_상태로_변경하면_예외가_발생한다() {
            // given
            given(memberMapper.lookupByIdForUpdate(TARGET_ID))
                    .willReturn(Optional.of(member(TARGET_ID, ACTOR_TEAM_ID, ClubRole.MEMBER, MemberStatus.ACTIVE)));
            givenActiveAdmin();

            // when & then
            assertThatThrownBy(() -> memberService.changeStatus(ADMIN_ID,
                    new StatusChangeParam(TARGET_ID, MemberStatus.ACTIVE, REASON)))
                    .isInstanceOf(NoChangeException.class);
        }
    }

    @Nested
    @DisplayName("멤버 관리 권한")
    class ManagementAuthorization {

        @Test
        void 활성_ADMIN이_아니면_팀을_변경할_수_없다() {
            // given
            given(memberMapper.lookupByIdForUpdate(ADMIN_ID)).willReturn(Optional.of(
                    member(ADMIN_ID, ACTOR_TEAM_ID, ClubRole.MEMBER, MemberStatus.ACTIVE)));
            given(memberMapper.lookupByIdForUpdate(TARGET_ID)).willReturn(Optional.of(
                    member(TARGET_ID, ACTOR_TEAM_ID, ClubRole.MEMBER, MemberStatus.ACTIVE)));

            // when & then
            assertThatThrownBy(() -> memberService.changeTeam(ADMIN_ID,
                    new TeamChangeParam(TARGET_ID, STAGE_TEAM_ID, REASON)))
                    .isInstanceOf(MemberManagementForbiddenException.class);
            verify(memberMapper, never()).updateTeam(any(), any());
        }

        @Test
        void 활성_ADMIN이_아니면_기수를_변경할_수_없다() {
            // given
            givenNonAdminActor();

            // when & then
            assertThatThrownBy(() -> memberService.changeCohort(ADMIN_ID,
                    new CohortChangeParam(TARGET_ID, NEW_COHORT_ID, REASON)))
                    .isInstanceOf(MemberManagementForbiddenException.class);
            verify(memberMapper, never()).updateCohort(any(), any());
        }

        @Test
        void 활성_ADMIN이_아니면_권한을_변경할_수_없다() {
            // given
            givenNonAdminActor();

            // when & then
            assertThatThrownBy(() -> memberService.changeRole(ADMIN_ID,
                    new RoleChangeParam(TARGET_ID, ClubRole.LEADER, REASON)))
                    .isInstanceOf(MemberManagementForbiddenException.class);
            verify(memberMapper, never()).updateRole(any(), any());
        }

        @Test
        void 활성_ADMIN이_아니면_상태를_변경할_수_없다() {
            // given
            givenNonAdminActor();

            // when & then
            assertThatThrownBy(() -> memberService.changeStatus(ADMIN_ID,
                    new StatusChangeParam(TARGET_ID, MemberStatus.SUSPENDED, REASON)))
                    .isInstanceOf(MemberManagementForbiddenException.class);
            verify(memberMapper, never()).updateStatus(any(), any());
        }
    }

    @Nested
    @DisplayName("내부 기능 접근 컨텍스트")
    class InternalAccessContext {

        @Test
        void 멤버의_현재_팀과_권한과_활성_상태를_반환한다() {
            given(memberMapper.lookupById(TARGET_ID))
                    .willReturn(Optional.of(member(TARGET_ID, STAGE_TEAM_ID,
                            ClubRole.LEADER, MemberStatus.ACTIVE)));

            MemberAccessContext context = memberService.lookupAccessContext(TARGET_ID);

            assertThat(context.memberId()).isEqualTo(TARGET_ID);
            assertThat(context.teamId()).isEqualTo(STAGE_TEAM_ID);
            assertThat(context.leader()).isTrue();
            assertThat(context.active()).isTrue();
        }

        @Test
        void 존재하지_않는_멤버의_접근_컨텍스트는_조회할_수_없다() {
            given(memberMapper.lookupById(TARGET_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> memberService.lookupAccessContext(TARGET_ID))
                    .isInstanceOf(MemberNotFoundException.class);
        }

        @Test
        void 활성_팀은_다른_feature가_사용하기_전에_검증할_수_있다() {
            given(teamMapper.lookupById(STAGE_TEAM_ID))
                    .willReturn(Optional.of(activeTeam(STAGE_TEAM_ID)));

            memberService.validateActiveTeam(STAGE_TEAM_ID);

            verify(teamMapper).lookupById(STAGE_TEAM_ID);
        }

        @Test
        void 비활성_팀은_다른_feature에서_사용할_수_없다() {
            given(teamMapper.lookupById(STAGE_TEAM_ID))
                    .willReturn(Optional.of(new Team(STAGE_TEAM_ID, "무대팀", 4, false)));

            assertThatThrownBy(() -> memberService.validateActiveTeam(STAGE_TEAM_ID))
                    .isInstanceOf(InactiveTeamException.class);
        }
    }
}
