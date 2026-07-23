package kr.ac.tukorea.bandi.domain.member.service;

import kr.ac.tukorea.bandi.domain.audit.service.AuditService;
import kr.ac.tukorea.bandi.domain.member.dto.response.SchoolConnectionResponse;
import kr.ac.tukorea.bandi.domain.member.exception.SchoolMemberNotRegisteredException;
import kr.ac.tukorea.bandi.domain.member.mapper.CohortMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.ClubOfficerMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.MemberHistoryMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.MemberMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.TeamMapper;
import kr.ac.tukorea.bandi.domain.member.model.AcademicStatus;
import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.Member;
import kr.ac.tukorea.bandi.domain.member.model.MemberSchoolConnection;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatusHistory;
import kr.ac.tukorea.bandi.domain.member.model.SchoolConnectionOutcome;
import kr.ac.tukorea.bandi.domain.member.model.SchoolIdentity;
import kr.ac.tukorea.bandi.domain.member.model.SsoLinkStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberSchoolConnectionServiceTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2026-07-18T08:30:00Z");
    private static final LocalDateTime VERIFIED_AT = LocalDateTime.of(2026, 7, 18, 17, 30);

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
        Clock clock = Clock.fixed(FIXED_INSTANT, ZoneId.of("Asia/Seoul"));
        memberService = new MemberService(memberMapper, teamMapper,
                cohortMapper, memberHistoryMapper, clubOfficerMapper,
                auditService, clock);
    }

    @Test
    void 학교_신원을_잠금_조회한_멤버에_반영하고_인증_응답을_반환한다() {
        // given
        Member member = preRegisteredMember();
        SchoolIdentity identity = enrolledIdentity("김하늘");
        given(memberMapper.lookupByStudentNoForUpdate("2021184000")).willReturn(Optional.of(member));

        // when
        SchoolConnectionResponse response = memberService.connectSchoolIdentity(identity);

        // then
        ArgumentCaptor<MemberSchoolConnection> captor = ArgumentCaptor.forClass(MemberSchoolConnection.class);
        verify(memberMapper).updateSchoolConnection(captor.capture());
        ArgumentCaptor<MemberStatusHistory> historyCaptor = ArgumentCaptor.forClass(MemberStatusHistory.class);
        verify(memberHistoryMapper).insertStatusHistory(historyCaptor.capture());
        assertThat(captor.getValue().academicStatusVerifiedDttm()).isEqualTo(VERIFIED_AT);
        assertThat(captor.getValue().outcome()).isEqualTo(SchoolConnectionOutcome.AUTHENTICATED);
        assertThat(response.memberId()).isEqualTo(member.getMemberId());
        assertThat(response.teamId()).isEqualTo(3L);
        assertThat(response.role()).isEqualTo(ClubRole.MEMBER);
        assertThat(response.outcome()).isEqualTo(SchoolConnectionOutcome.AUTHENTICATED);
        assertThat(historyCaptor.getValue().previousStatus()).isEqualTo(MemberStatus.PRE_REGISTERED);
        assertThat(historyCaptor.getValue().newStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(historyCaptor.getValue().changedByMemberId()).isEqualTo(200L);
    }

    @Test
    void 등록되지_않은_학번은_연결할_수_없다() {
        // given
        SchoolIdentity identity = enrolledIdentity("김하늘");
        given(memberMapper.lookupByStudentNoForUpdate("2021184000")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.connectSchoolIdentity(identity))
                .isInstanceOf(SchoolMemberNotRegisteredException.class);
    }

    @Test
    void 이름_불일치_결과도_검토_상태로_DB에_반영한다() {
        // given
        Member member = preRegisteredMember();
        given(memberMapper.lookupByStudentNoForUpdate("2021184000")).willReturn(Optional.of(member));

        // when
        SchoolConnectionResponse response = memberService.connectSchoolIdentity(enrolledIdentity("김바다"));

        // then
        ArgumentCaptor<MemberSchoolConnection> captor = ArgumentCaptor.forClass(MemberSchoolConnection.class);
        verify(memberMapper).updateSchoolConnection(captor.capture());
        assertThat(captor.getValue().outcome()).isEqualTo(SchoolConnectionOutcome.IDENTITY_REVIEW_REQUIRED);
        assertThat(response.outcome()).isEqualTo(SchoolConnectionOutcome.IDENTITY_REVIEW_REQUIRED);
        verify(memberHistoryMapper, never()).insertStatusHistory(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void 이미_연결된_활성_멤버의_재로그인은_상태_이력을_중복_생성하지_않는다() {
        // given
        Member member = new Member(200L, "2021184000", "김하늘", "컴퓨터공학부",
                AcademicStatus.ENROLLED, VERIFIED_AT.minusDays(1), 3L, 1L, ClubRole.MEMBER,
                MemberStatus.ACTIVE, SsoLinkStatus.LINKED, VERIFIED_AT.minusMonths(1),
                VERIFIED_AT.minusDays(1), 100L);
        given(memberMapper.lookupByStudentNoForUpdate("2021184000")).willReturn(Optional.of(member));

        // when
        memberService.connectSchoolIdentity(enrolledIdentity("김하늘"));

        // then
        verify(memberHistoryMapper, never()).insertStatusHistory(org.mockito.ArgumentMatchers.any());
    }

    private SchoolIdentity enrolledIdentity(String name) {
        return new SchoolIdentity("2021184000", name, "컴퓨터공학부", AcademicStatus.ENROLLED);
    }

    private Member preRegisteredMember() {
        return new Member(200L, "2021184000", "김하늘", null, null, null,
                3L, 1L, ClubRole.MEMBER, MemberStatus.PRE_REGISTERED,
                SsoLinkStatus.WAITING, null, null, 100L);
    }
}
