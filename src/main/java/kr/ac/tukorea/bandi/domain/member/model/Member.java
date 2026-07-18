package kr.ac.tukorea.bandi.domain.member.model;

import kr.ac.tukorea.bandi.domain.member.exception.InvalidMemberStatusTransitionException;
import kr.ac.tukorea.bandi.domain.member.exception.NoChangeException;
import kr.ac.tukorea.bandi.domain.member.exception.SelfRoleDemotionException;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public class Member {

    private Long memberId;
    private final String studentNo;
    private final String name;
    private final String department;
    private final String academicStatusCode;
    private final LocalDateTime academicStatusVerifiedDttm;
    private final Long teamId;
    private final Long cohortId;
    private final ClubRole role;
    private final MemberStatus status;
    private final SsoLinkStatus ssoLinkStatus;
    private final LocalDateTime ssoLinkedDttm;
    private final LocalDateTime lastLoginDttm;
    private final Long registeredByMemberId;

    public Member(Long memberId, String studentNo, String name, String department,
                  String academicStatusCode, LocalDateTime academicStatusVerifiedDttm,
                  Long teamId, Long cohortId, ClubRole role, MemberStatus status,
                  SsoLinkStatus ssoLinkStatus, LocalDateTime ssoLinkedDttm,
                  LocalDateTime lastLoginDttm, Long registeredByMemberId) {
        this.memberId = memberId;
        this.studentNo = studentNo;
        this.name = name;
        this.department = department;
        this.academicStatusCode = academicStatusCode;
        this.academicStatusVerifiedDttm = academicStatusVerifiedDttm;
        this.teamId = teamId;
        this.cohortId = cohortId;
        this.role = role;
        this.status = status;
        this.ssoLinkStatus = ssoLinkStatus;
        this.ssoLinkedDttm = ssoLinkedDttm;
        this.lastLoginDttm = lastLoginDttm;
        this.registeredByMemberId = registeredByMemberId;
    }

    /**
     * 운영진이 합격자를 사전 등록한 상태의 멤버.
     * 학적·SSO 연결·로그인 정보는 최초 학교 인증이 채우므로 비워 둔다 (정본 5.3).
     */
    public static Member preRegister(String studentNo, String name, Long teamId, Long cohortId,
                                     ClubRole role, Long registeredByMemberId) {
        return new Member(null, studentNo, name, null, null, null,
                teamId, cohortId, role, MemberStatus.PRE_REGISTERED,
                SsoLinkStatus.WAITING, null, null, registeredByMemberId);
    }

    public void validateTeamChangeTo(Long newTeamId) {
        if (Objects.equals(teamId, newTeamId)) {
            throw new NoChangeException("team");
        }
    }

    public void validateCohortChangeTo(Long newCohortId) {
        if (Objects.equals(cohortId, newCohortId)) {
            throw new NoChangeException("cohort");
        }
    }

    /**
     * 정본 5.4 — 본인의 ADMIN 권한 하향은 다른 ADMIN만 실행할 수 있다.
     * ADMIN이 최상위 권한이므로 ADMIN에서 벗어나는 변경은 모두 하향이다.
     */
    public void validateRoleChangeTo(ClubRole newRole, Long actorMemberId) {
        if (role == newRole) {
            throw new NoChangeException("role");
        }
        if (isAdmin() && Objects.equals(memberId, actorMemberId)) {
            throw new SelfRoleDemotionException(memberId);
        }
    }

    public void validateManagementStatusChangeTo(MemberStatus newStatus) {
        if (status == newStatus) {
            throw new NoChangeException("status");
        }
        if (!status.canTransitionByAdminTo(newStatus)) {
            throw new InvalidMemberStatusTransitionException(status, newStatus);
        }
    }

    public boolean isAdmin() {
        return role == ClubRole.ADMIN;
    }

    /** 마지막 운영진 보호 규칙이 세는 대상 — 권한과 활동 상태를 모두 만족해야 한다. */
    public boolean isActiveAdmin() {
        return isAdmin() && status == MemberStatus.ACTIVE;
    }
}
