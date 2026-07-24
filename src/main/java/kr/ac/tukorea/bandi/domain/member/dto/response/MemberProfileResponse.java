package kr.ac.tukorea.bandi.domain.member.dto.response;

import kr.ac.tukorea.bandi.domain.member.model.AcademicStatus;
import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.Member;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;
import kr.ac.tukorea.bandi.domain.member.model.SsoLinkStatus;

import java.time.LocalDateTime;

public record MemberProfileResponse(
        Long memberId,
        String studentNo,
        String name,
        Long teamId,
        String teamName,
        String cohortName,
        ClubRole role,
        MemberStatus status,
        String department,
        String phoneNumber,
        AcademicStatus academicStatus,
        LocalDateTime academicStatusVerifiedDttm,
        SsoLinkStatus ssoLinkStatus,
        LocalDateTime ssoLinkedDttm,
        LocalDateTime lastLoginDttm,
        boolean hasProfilePhoto
) {

    public MemberProfileResponse(Long memberId, String studentNo, String name,
                                 Long teamId, String teamName, String cohortName,
                                 ClubRole role, MemberStatus status, String department,
                                 AcademicStatus academicStatus,
                                 LocalDateTime academicStatusVerifiedDttm,
                                 SsoLinkStatus ssoLinkStatus,
                                 LocalDateTime ssoLinkedDttm,
                                 LocalDateTime lastLoginDttm,
                                 boolean hasProfilePhoto) {
        this(memberId, studentNo, name, teamId, teamName, cohortName, role, status,
                department, null, academicStatus, academicStatusVerifiedDttm,
                ssoLinkStatus, ssoLinkedDttm, lastLoginDttm, hasProfilePhoto);
    }

    public static MemberProfileResponse from(Member member, String teamName,
                                             String cohortName) {
        return new MemberProfileResponse(member.getMemberId(), member.getStudentNo(),
                member.getName(), member.getTeamId(), teamName, cohortName, member.getRole(),
                member.getStatus(), member.getDepartment(), member.getPhoneNumber(), member.getAcademicStatus(),
                member.getAcademicStatusVerifiedDttm(), member.getSsoLinkStatus(),
                member.getSsoLinkedDttm(), member.getLastLoginDttm(),
                member.getProfilePhotoFileId() != null);
    }

    public MemberProfileResponse withProfilePhoto(boolean hasProfilePhoto) {
        return new MemberProfileResponse(memberId, studentNo, name, teamId, teamName, cohortName,
                role, status, department, phoneNumber, academicStatus, academicStatusVerifiedDttm,
                ssoLinkStatus, ssoLinkedDttm, lastLoginDttm, hasProfilePhoto);
    }
}
