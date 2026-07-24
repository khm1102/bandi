package kr.ac.tukorea.bandi.domain.member.dto.response;

import kr.ac.tukorea.bandi.domain.member.model.AcademicStatus;
import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.Member;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import kr.ac.tukorea.bandi.domain.member.model.SsoLinkStatus;

import java.time.LocalDateTime;

public record MemberResponse(
        Long memberId,
        String studentNo,
        String name,
        String department,
        String phoneNumber,
        AcademicStatus academicStatus,
        LocalDateTime academicStatusVerifiedDttm,
        Long teamId,
        Long cohortId,
        ClubRole role,
        MemberStatus status,
        SsoLinkStatus ssoLinkStatus,
        LocalDateTime ssoLinkedDttm,
        LocalDateTime lastLoginDttm,
        Long registeredByMemberId,
        Long profilePhotoFileId
) {

    public MemberResponse(Long memberId, String studentNo, String name,
                          String department, AcademicStatus academicStatus,
                          LocalDateTime academicStatusVerifiedDttm, Long teamId,
                          Long cohortId, ClubRole role, MemberStatus status,
                          SsoLinkStatus ssoLinkStatus, LocalDateTime ssoLinkedDttm,
                          LocalDateTime lastLoginDttm, Long registeredByMemberId) {
        this(memberId, studentNo, name, department, null, academicStatus,
                academicStatusVerifiedDttm, teamId, cohortId, role, status,
                ssoLinkStatus, ssoLinkedDttm, lastLoginDttm, registeredByMemberId, null);
    }

    public MemberResponse(Long memberId, String studentNo, String name,
                          String department, AcademicStatus academicStatus,
                          LocalDateTime academicStatusVerifiedDttm, Long teamId,
                          Long cohortId, ClubRole role, MemberStatus status,
                          SsoLinkStatus ssoLinkStatus, LocalDateTime ssoLinkedDttm,
                          LocalDateTime lastLoginDttm, Long registeredByMemberId,
                          Long profilePhotoFileId) {
        this(memberId, studentNo, name, department, null, academicStatus,
                academicStatusVerifiedDttm, teamId, cohortId, role, status,
                ssoLinkStatus, ssoLinkedDttm, lastLoginDttm, registeredByMemberId,
                profilePhotoFileId);
    }

    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getMemberId(), member.getStudentNo(),
                member.getName(), member.getDepartment(), member.getPhoneNumber(),
                member.getAcademicStatus(),
                member.getAcademicStatusVerifiedDttm(), member.getTeamId(),
                member.getCohortId(), member.getRole(), member.getStatus(),
                member.getSsoLinkStatus(), member.getSsoLinkedDttm(),
                member.getLastLoginDttm(), member.getRegisteredByMemberId(),
                member.getProfilePhotoFileId());
    }
}
