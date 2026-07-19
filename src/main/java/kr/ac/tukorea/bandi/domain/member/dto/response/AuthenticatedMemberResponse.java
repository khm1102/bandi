package kr.ac.tukorea.bandi.domain.member.dto.response;

import kr.ac.tukorea.bandi.domain.member.model.ClubRole;

public record AuthenticatedMemberResponse(
        Long memberId,
        Long teamId,
        ClubRole role
) {

    public static AuthenticatedMemberResponse from(SchoolConnectionResponse connection) {
        return new AuthenticatedMemberResponse(connection.memberId(), connection.teamId(), connection.role());
    }
}
