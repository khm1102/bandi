package kr.ac.tukorea.bandi.domain.member.dto.response;

import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.Member;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;

public record TeamMemberResponse(
        Long memberId,
        String name,
        String studentNo,
        String teamName,
        ClubRole role,
        MemberStatus status,
        boolean hasProfilePhoto
) {

    public static TeamMemberResponse from(Member member, String teamName) {
        return new TeamMemberResponse(member.getMemberId(), member.getName(),
                member.getStudentNo(), teamName, member.getRole(), member.getStatus(),
                member.getProfilePhotoFileId() != null);
    }
}
