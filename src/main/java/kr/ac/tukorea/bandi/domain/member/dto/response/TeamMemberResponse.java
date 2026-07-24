package kr.ac.tukorea.bandi.domain.member.dto.response;

import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.Member;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;

public record TeamMemberResponse(
        Long memberId,
        String name,
        String studentNo,
        Long teamId,
        String teamName,
        String cohortName,
        String phoneNumber,
        ClubRole role,
        MemberStatus status,
        boolean hasProfilePhoto
) {

    public TeamMemberResponse(Long memberId, String name, String studentNo,
                              Long teamId, String teamName, ClubRole role,
                              MemberStatus status, boolean hasProfilePhoto) {
        this(memberId, name, studentNo, teamId, teamName, null, null, role, status, hasProfilePhoto);
    }

    public static TeamMemberResponse from(Member member, String teamName, String cohortName) {
        return new TeamMemberResponse(member.getMemberId(), member.getName(),
                member.getStudentNo(), member.getTeamId(), teamName, cohortName, member.getPhoneNumber(),
                member.getRole(), member.getStatus(),
                member.getProfilePhotoFileId() != null);
    }
}
