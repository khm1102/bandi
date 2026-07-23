package kr.ac.tukorea.bandi.domain.member.service;

import kr.ac.tukorea.bandi.domain.audit.service.AuditService;
import kr.ac.tukorea.bandi.domain.member.exception.ClubPresidentUnavailableException;
import kr.ac.tukorea.bandi.domain.member.mapper.ClubOfficerMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.CohortMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.MemberHistoryMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.MemberMapper;
import kr.ac.tukorea.bandi.domain.member.mapper.TeamMapper;
import kr.ac.tukorea.bandi.domain.member.model.AcademicStatus;
import kr.ac.tukorea.bandi.domain.member.model.ClubOfficerPosition;
import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.Member;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import kr.ac.tukorea.bandi.domain.member.model.SsoLinkStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MemberOfficerServiceTest {

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
        memberService = new MemberService(memberMapper, teamMapper, cohortMapper,
                memberHistoryMapper, clubOfficerMapper, auditService,
                Clock.systemUTC());
    }

    @Test
    void 현재_활성_회장_이름을_조회한다() {
        given(clubOfficerMapper.lookupActiveMemberNameByPosition(
                ClubOfficerPosition.PRESIDENT)).willReturn(Optional.of("원동연"));

        String name = memberService.lookupActivePresidentName();

        assertThat(name).isEqualTo("원동연");
    }

    @Test
    void 현재_활성_회장이_없으면_문서_생성을_차단한다() {
        given(clubOfficerMapper.lookupActiveMemberNameByPosition(
                ClubOfficerPosition.PRESIDENT)).willReturn(Optional.empty());

        assertThatThrownBy(memberService::lookupActivePresidentName)
                .isInstanceOf(ClubPresidentUnavailableException.class);
    }

    @Test
    void 활동_내역서_참여자_검색은_활성_멤버_열_명으로_제한한다() {
        Member member = new Member(1L, "2025591010", "김현민", "컴퓨터공학부",
                AcademicStatus.ENROLLED, null, 1L, 1L, ClubRole.MEMBER,
                MemberStatus.ACTIVE, SsoLinkStatus.LINKED, null, null, null);
        given(memberMapper.searchActiveByKeyword("김현", 10))
                .willReturn(List.of(member));

        List<MemberService.ActivityReportParticipantLookup> result =
                memberService.searchActivityReportParticipants("김현");

        assertThat(result).containsExactly(new MemberService.ActivityReportParticipantLookup(
                "김현민", "컴퓨터공학부", "2025591010"));
        verify(memberMapper).searchActiveByKeyword("김현", 10);
    }
}
