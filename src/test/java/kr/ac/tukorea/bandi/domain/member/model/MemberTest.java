package kr.ac.tukorea.bandi.domain.member.model;

import kr.ac.tukorea.bandi.domain.member.exception.NoChangeException;
import kr.ac.tukorea.bandi.domain.member.exception.SelfRoleDemotionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberTest {

    private static final Long ACTOR_TEAM_ID = 3L;
    private static final Long STAGE_TEAM_ID = 4L;
    private static final Long COHORT_ID = 1L;
    private static final Long REGISTRAR_ID = 100L;

    /**
     * 저장 후 식별자를 가진 멤버. MyBatis 매핑용 전체 생성자는 여기서만 호출해
     * 테스트 본문이 인자 순서에 휘둘리지 않게 한다.
     */
    private static Member savedMember(Long memberId, ClubRole role, MemberStatus status) {
        return new Member(memberId, "2020184000", "이서준", "컴퓨터공학부", "ENROLLED", null,
                ACTOR_TEAM_ID, COHORT_ID, role, status, SsoLinkStatus.LINKED, null, null, REGISTRAR_ID);
    }

    @Nested
    @DisplayName("사전 등록")
    class PreRegister {

        @Test
        void 사전_등록한_멤버는_PRE_REGISTERED와_WAITING_상태로_시작한다() {
            // given & when
            Member member = Member.preRegister("2021184000", "김하늘", ACTOR_TEAM_ID, COHORT_ID,
                    ClubRole.MEMBER, REGISTRAR_ID);

            // then
            assertThat(member.getStatus()).isEqualTo(MemberStatus.PRE_REGISTERED);
            assertThat(member.getSsoLinkStatus()).isEqualTo(SsoLinkStatus.WAITING);
        }

        @Test
        void 사전_등록한_멤버는_학교_학적_정보와_로그인_이력이_비어_있다() {
            // given & when
            Member member = Member.preRegister("2021184000", "김하늘", ACTOR_TEAM_ID, COHORT_ID,
                    ClubRole.MEMBER, REGISTRAR_ID);

            // then — 학적과 로그인 시각은 학교 SSO 연결이 채운다
            assertThat(member.getAcademicStatusCode()).isNull();
            assertThat(member.getSsoLinkedDttm()).isNull();
            assertThat(member.getLastLoginDttm()).isNull();
        }
    }

    @Nested
    @DisplayName("팀 변경")
    class TeamChange {

        @Test
        void 동일한_팀으로_변경하면_예외가_발생한다() {
            // given
            Member member = savedMember(1L, ClubRole.MEMBER, MemberStatus.ACTIVE);

            // when & then
            assertThatThrownBy(() -> member.validateTeamChangeTo(ACTOR_TEAM_ID))
                    .isInstanceOf(NoChangeException.class);
        }

        @Test
        void 다른_팀으로_변경하면_검증을_통과한다() {
            // given
            Member member = savedMember(1L, ClubRole.MEMBER, MemberStatus.ACTIVE);

            // when & then
            assertThatCode(() -> member.validateTeamChangeTo(STAGE_TEAM_ID)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("기수 변경")
    class CohortChange {

        @Test
        void 동일한_기수로_변경하면_예외가_발생한다() {
            // given
            Member member = savedMember(1L, ClubRole.MEMBER, MemberStatus.ACTIVE);

            // when & then
            assertThatThrownBy(() -> member.validateCohortChangeTo(COHORT_ID))
                    .isInstanceOf(NoChangeException.class);
        }

        @Test
        void 다른_기수로_변경하면_검증을_통과한다() {
            // given
            Member member = savedMember(1L, ClubRole.MEMBER, MemberStatus.ACTIVE);

            // when & then
            assertThatCode(() -> member.validateCohortChangeTo(2L)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("권한 변경")
    class RoleChange {

        @Test
        void 동일한_역할로_변경하면_예외가_발생한다() {
            // given
            Member member = savedMember(1L, ClubRole.MEMBER, MemberStatus.ACTIVE);

            // when & then
            assertThatThrownBy(() -> member.validateRoleChangeTo(ClubRole.MEMBER, REGISTRAR_ID))
                    .isInstanceOf(NoChangeException.class);
        }

        @Test
        void ADMIN이_본인의_권한을_낮추면_예외가_발생한다() {
            // given
            Member admin = savedMember(1L, ClubRole.ADMIN, MemberStatus.ACTIVE);

            // when & then
            assertThatThrownBy(() -> admin.validateRoleChangeTo(ClubRole.MEMBER, 1L))
                    .isInstanceOf(SelfRoleDemotionException.class);
        }

        @Test
        void 다른_ADMIN이_대상_ADMIN의_권한을_낮추면_검증을_통과한다() {
            // given
            Member targetAdmin = savedMember(1L, ClubRole.ADMIN, MemberStatus.ACTIVE);
            Long otherAdminId = 2L;

            // when & then
            assertThatCode(() -> targetAdmin.validateRoleChangeTo(ClubRole.MEMBER, otherAdminId))
                    .doesNotThrowAnyException();
        }

        @Test
        void ADMIN이_아닌_멤버는_본인_권한을_직접_바꿔도_예외가_아니다() {
            // given — 자기 강등 제한은 마지막 운영진 보호가 목적이므로 ADMIN에만 적용한다
            Member leader = savedMember(1L, ClubRole.LEADER, MemberStatus.ACTIVE);

            // when & then
            assertThatCode(() -> leader.validateRoleChangeTo(ClubRole.MEMBER, 1L)).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("상태 변경")
    class StatusChange {

        @Test
        void 같은_상태로_변경하면_예외가_발생한다() {
            // given
            Member member = savedMember(1L, ClubRole.MEMBER, MemberStatus.ACTIVE);

            // when & then
            assertThatThrownBy(() -> member.validateStatusChangeTo(MemberStatus.ACTIVE))
                    .isInstanceOf(NoChangeException.class);
        }

        @Test
        void 다른_상태로_변경하면_검증을_통과한다() {
            // given
            Member member = savedMember(1L, ClubRole.MEMBER, MemberStatus.PRE_REGISTERED);

            // when & then
            assertThatCode(() -> member.validateStatusChangeTo(MemberStatus.REGISTRATION_CANCELLED))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("운영진 판단")
    class AdminDecision {

        @Test
        void ADMIN이면서_ACTIVE인_멤버는_활성_운영진이다() {
            // given
            Member activeAdmin = savedMember(1L, ClubRole.ADMIN, MemberStatus.ACTIVE);

            // when & then
            assertThat(activeAdmin.isActiveAdmin()).isTrue();
        }

        @Test
        void ADMIN이어도_활동_중지_상태면_활성_운영진이_아니다() {
            // given
            Member suspendedAdmin = savedMember(1L, ClubRole.ADMIN, MemberStatus.SUSPENDED);

            // when & then
            assertThat(suspendedAdmin.isAdmin()).isTrue();
            assertThat(suspendedAdmin.isActiveAdmin()).isFalse();
        }

        @Test
        void 사전_등록만_된_ADMIN은_아직_활성_운영진이_아니다() {
            // given
            Member preRegisteredAdmin = Member.preRegister("2020184000", "이서준", ACTOR_TEAM_ID, COHORT_ID,
                    ClubRole.ADMIN, null);

            // when & then
            assertThat(preRegisteredAdmin.isActiveAdmin()).isFalse();
        }
    }
}
