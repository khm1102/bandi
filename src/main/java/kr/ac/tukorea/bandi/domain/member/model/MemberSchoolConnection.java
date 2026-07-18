package kr.ac.tukorea.bandi.domain.member.model;

import java.time.LocalDateTime;

/** 학교 인증 결과를 member 현재값에 반영하기 위한 불변 스냅샷. */
public record MemberSchoolConnection(
        Long memberId,
        String department,
        AcademicStatus academicStatus,
        LocalDateTime academicStatusVerifiedDttm,
        MemberStatus memberStatus,
        SsoLinkStatus ssoLinkStatus,
        LocalDateTime ssoLinkedDttm,
        LocalDateTime lastLoginDttm,
        SchoolConnectionOutcome outcome
) {

    public boolean changesMemberStatusFrom(Member member) {
        return memberStatus != member.getStatus();
    }

    public MemberStatusHistory toStatusHistory(Member member, String reason) {
        return MemberStatusHistory.of(memberId, member.getStatus(), memberStatus,
                reason, memberId, academicStatusVerifiedDttm);
    }

    public static MemberSchoolConnection of(
            Member member,
            SchoolIdentity identity,
            LocalDateTime verifiedAt,
            MemberStatus memberStatus,
            SsoLinkStatus ssoLinkStatus,
            LocalDateTime ssoLinkedDttm,
            LocalDateTime lastLoginDttm,
            SchoolConnectionOutcome outcome
    ) {
        return new MemberSchoolConnection(member.getMemberId(), identity.department(), identity.academicStatus(),
                verifiedAt, memberStatus, ssoLinkStatus, ssoLinkedDttm, lastLoginDttm, outcome);
    }
}
