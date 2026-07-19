package kr.ac.tukorea.bandi.domain.member.service;

import kr.ac.tukorea.bandi.domain.member.client.SchoolCredentials;
import kr.ac.tukorea.bandi.domain.member.client.SchoolSsoClient;
import kr.ac.tukorea.bandi.domain.member.dto.response.AuthenticatedMemberResponse;
import kr.ac.tukorea.bandi.domain.member.dto.response.SchoolConnectionResponse;
import kr.ac.tukorea.bandi.domain.member.exception.MemberLoginDeniedException;
import kr.ac.tukorea.bandi.domain.member.exception.SchoolAcademicStatusDeniedException;
import kr.ac.tukorea.bandi.domain.member.exception.SchoolIdentityMismatchException;
import kr.ac.tukorea.bandi.domain.member.exception.SchoolIdentityReviewRequiredException;
import kr.ac.tukorea.bandi.domain.member.model.AcademicStatus;
import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.SchoolConnectionOutcome;
import kr.ac.tukorea.bandi.domain.member.model.SchoolIdentity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class SchoolAuthenticationServiceTest {

    @Mock
    private SchoolSsoClient schoolSsoClient;
    @Mock
    private MemberService memberService;

    private SchoolAuthenticationService schoolAuthenticationService;

    @BeforeEach
    void setUp() {
        schoolAuthenticationService = new SchoolAuthenticationService(schoolSsoClient, memberService);
    }

    @Test
    void 학교_인증과_멤버_연결이_모두_성공하면_세션용_최소_정보를_반환한다() {
        // given
        SchoolCredentials credentials = new SchoolCredentials("2021184000", "school-password");
        SchoolIdentity identity = enrolledIdentity();
        given(schoolSsoClient.authenticate(credentials)).willReturn(identity);
        given(memberService.connectSchoolIdentity(identity)).willReturn(connection(SchoolConnectionOutcome.AUTHENTICATED));

        // when
        AuthenticatedMemberResponse response = schoolAuthenticationService.authenticate(credentials);

        // then
        assertThat(response.memberId()).isEqualTo(200L);
        assertThat(response.teamId()).isEqualTo(3L);
        assertThat(response.role()).isEqualTo(ClubRole.MEMBER);
        verify(memberService).connectSchoolIdentity(identity);
    }

    @Test
    void 비재학생은_학교_확인값을_저장한_뒤_로그인을_거부한다() {
        assertDenied(SchoolConnectionOutcome.ACADEMIC_STATUS_DENIED, SchoolAcademicStatusDeniedException.class);
    }

    @Test
    void 이름_불일치는_검토_상태를_저장한_뒤_로그인을_거부한다() {
        assertDenied(SchoolConnectionOutcome.IDENTITY_REVIEW_REQUIRED,
                SchoolIdentityReviewRequiredException.class);
    }

    @Test
    void 활동_중지나_탈퇴_멤버는_로그인을_거부한다() {
        assertDenied(SchoolConnectionOutcome.MEMBER_STATUS_DENIED, MemberLoginDeniedException.class);
    }

    @Test
    void 자격증명_문자열_표현에는_학번과_비밀번호가_포함되지_않는다() {
        // given
        SchoolCredentials credentials = new SchoolCredentials("2021184000", "school-password");

        // when
        String rendered = credentials.toString();

        // then
        assertThat(rendered).doesNotContain("2021184000", "school-password");
    }

    @Test
    void 입력한_학번과_학교가_반환한_학번이_다르면_멤버를_연결하지_않는다() {
        // given
        SchoolCredentials credentials = new SchoolCredentials("2021184000", "school-password");
        SchoolIdentity differentIdentity = new SchoolIdentity(
                "2021184999", "김하늘", "컴퓨터공학부", AcademicStatus.ENROLLED);
        given(schoolSsoClient.authenticate(credentials)).willReturn(differentIdentity);

        // when & then
        assertThatThrownBy(() -> schoolAuthenticationService.authenticate(credentials))
                .isInstanceOf(SchoolIdentityMismatchException.class);
        verifyNoInteractions(memberService);
    }

    private void assertDenied(SchoolConnectionOutcome outcome,
                              Class<? extends RuntimeException> exceptionType) {
        SchoolCredentials credentials = new SchoolCredentials("2021184000", "school-password");
        SchoolIdentity identity = enrolledIdentity();
        given(schoolSsoClient.authenticate(credentials)).willReturn(identity);
        given(memberService.connectSchoolIdentity(identity)).willReturn(connection(outcome));

        assertThatThrownBy(() -> schoolAuthenticationService.authenticate(credentials))
                .isInstanceOf(exceptionType);
        verify(memberService).connectSchoolIdentity(identity);
    }

    private SchoolIdentity enrolledIdentity() {
        return new SchoolIdentity("2021184000", "김하늘", "컴퓨터공학부", AcademicStatus.ENROLLED);
    }

    private SchoolConnectionResponse connection(SchoolConnectionOutcome outcome) {
        return new SchoolConnectionResponse(200L, 3L, ClubRole.MEMBER, AcademicStatus.ENROLLED, outcome);
    }
}
