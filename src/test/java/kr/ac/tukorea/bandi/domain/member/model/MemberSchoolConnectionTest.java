package kr.ac.tukorea.bandi.domain.member.model;

import kr.ac.tukorea.bandi.domain.member.exception.SchoolIdentityMismatchException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberSchoolConnectionTest {

    private static final LocalDateTime PREVIOUS_LINKED_AT = LocalDateTime.of(2026, 3, 2, 9, 0);
    private static final LocalDateTime PREVIOUS_LOGIN_AT = LocalDateTime.of(2026, 7, 1, 12, 0);
    private static final LocalDateTime VERIFIED_AT = LocalDateTime.of(2026, 7, 18, 17, 30);

    @Test
    void 사전_등록_정보와_재학생_신원이_일치하면_최초_연결하고_활성화한다() {
        // given
        Member member = Member.preRegister("2021184000", "김하늘", 3L, 1L, ClubRole.MEMBER, 100L);
        SchoolIdentity identity = new SchoolIdentity(
                "2021184000", "김하늘", "컴퓨터공학부", AcademicStatus.ENROLLED);

        // when
        MemberSchoolConnection connection = member.determineSchoolConnection(identity, VERIFIED_AT);

        // then
        assertThat(connection.outcome()).isEqualTo(SchoolConnectionOutcome.AUTHENTICATED);
        assertThat(connection.memberStatus()).isEqualTo(MemberStatus.ACTIVE);
        assertThat(connection.ssoLinkStatus()).isEqualTo(SsoLinkStatus.LINKED);
        assertThat(connection.ssoLinkedDttm()).isEqualTo(VERIFIED_AT);
        assertThat(connection.lastLoginDttm()).isEqualTo(VERIFIED_AT);
        assertThat(connection.academicStatus()).isEqualTo(AcademicStatus.ENROLLED);
        assertThat(connection.department()).isEqualTo("컴퓨터공학부");
    }

    @Test
    void 연결된_활성_멤버가_재로그인하면_최초_연결_시각은_유지하고_마지막_로그인만_갱신한다() {
        // given
        Member member = linkedMember(MemberStatus.ACTIVE, SsoLinkStatus.LINKED);
        SchoolIdentity identity = enrolledIdentity("이서준");

        // when
        MemberSchoolConnection connection = member.determineSchoolConnection(identity, VERIFIED_AT);

        // then
        assertThat(connection.outcome()).isEqualTo(SchoolConnectionOutcome.AUTHENTICATED);
        assertThat(connection.ssoLinkedDttm()).isEqualTo(PREVIOUS_LINKED_AT);
        assertThat(connection.lastLoginDttm()).isEqualTo(VERIFIED_AT);
    }

    @Test
    void 등록_이름과_학교_이름이_다르면_검토_대상으로_전환하고_활성화하지_않는다() {
        // given
        Member member = Member.preRegister("2021184000", "김하늘", 3L, 1L, ClubRole.MEMBER, 100L);
        SchoolIdentity identity = enrolledIdentity("김바다");

        // when
        MemberSchoolConnection connection = member.determineSchoolConnection(identity, VERIFIED_AT);

        // then
        assertThat(connection.outcome()).isEqualTo(SchoolConnectionOutcome.IDENTITY_REVIEW_REQUIRED);
        assertThat(connection.memberStatus()).isEqualTo(MemberStatus.PRE_REGISTERED);
        assertThat(connection.ssoLinkStatus()).isEqualTo(SsoLinkStatus.REVIEW_REQUIRED);
        assertThat(connection.ssoLinkedDttm()).isNull();
        assertThat(connection.lastLoginDttm()).isNull();
    }

    @Test
    void 재학생이_아니면_학적은_기록하지만_멤버를_활성화하지_않는다() {
        // given
        Member member = Member.preRegister("2021184000", "김하늘", 3L, 1L, ClubRole.MEMBER, 100L);
        SchoolIdentity identity = new SchoolIdentity(
                "2021184000", "김하늘", "컴퓨터공학부", AcademicStatus.LEAVE_OF_ABSENCE);

        // when
        MemberSchoolConnection connection = member.determineSchoolConnection(identity, VERIFIED_AT);

        // then
        assertThat(connection.outcome()).isEqualTo(SchoolConnectionOutcome.ACADEMIC_STATUS_DENIED);
        assertThat(connection.academicStatus()).isEqualTo(AcademicStatus.LEAVE_OF_ABSENCE);
        assertThat(connection.memberStatus()).isEqualTo(MemberStatus.PRE_REGISTERED);
        assertThat(connection.ssoLinkStatus()).isEqualTo(SsoLinkStatus.WAITING);
        assertThat(connection.lastLoginDttm()).isNull();
    }

    @Test
    void 활동_중지_멤버는_학교_인증에_성공해도_로그인할_수_없다() {
        // given
        Member member = linkedMember(MemberStatus.SUSPENDED, SsoLinkStatus.LINKED);

        // when
        MemberSchoolConnection connection = member.determineSchoolConnection(enrolledIdentity("이서준"), VERIFIED_AT);

        // then
        assertThat(connection.outcome()).isEqualTo(SchoolConnectionOutcome.MEMBER_STATUS_DENIED);
        assertThat(connection.memberStatus()).isEqualTo(MemberStatus.SUSPENDED);
        assertThat(connection.ssoLinkStatus()).isEqualTo(SsoLinkStatus.LINKED);
        assertThat(connection.ssoLinkedDttm()).isEqualTo(PREVIOUS_LINKED_AT);
        assertThat(connection.lastLoginDttm()).isEqualTo(PREVIOUS_LOGIN_AT);
    }

    @Test
    void 조회한_멤버와_학교_학번이_다르면_연결을_거부한다() {
        // given
        Member member = Member.preRegister("2021184000", "김하늘", 3L, 1L, ClubRole.MEMBER, 100L);
        SchoolIdentity identity = new SchoolIdentity(
                "2021184999", "김하늘", "컴퓨터공학부", AcademicStatus.ENROLLED);

        // when & then
        assertThatThrownBy(() -> member.determineSchoolConnection(identity, VERIFIED_AT))
                .isInstanceOf(SchoolIdentityMismatchException.class);
    }

    private Member linkedMember(MemberStatus status, SsoLinkStatus linkStatus) {
        return new Member(200L, "2021184000", "이서준", "컴퓨터공학부", AcademicStatus.ENROLLED,
                PREVIOUS_LOGIN_AT, 3L, 1L, ClubRole.MEMBER, status, linkStatus,
                PREVIOUS_LINKED_AT, PREVIOUS_LOGIN_AT, 100L);
    }

    private SchoolIdentity enrolledIdentity(String name) {
        return new SchoolIdentity("2021184000", name, "컴퓨터공학부", AcademicStatus.ENROLLED);
    }
}
