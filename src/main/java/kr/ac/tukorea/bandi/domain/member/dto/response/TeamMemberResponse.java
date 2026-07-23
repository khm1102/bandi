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
        Long cohortId,
        String cohortName,
        ClubRole role,
        MemberStatus status,
        boolean hasProfilePhoto
) {

    public static TeamMemberResponse from(Member member, String teamName, String cohortName) {
        return new TeamMemberResponse(member.getMemberId(), member.getName(),
                member.getStudentNo(), member.getTeamId(), teamName, member.getCohortId(), cohortName,
                member.getRole(), member.getStatus(),
                member.getProfilePhotoFileId() != null);
    }
}
