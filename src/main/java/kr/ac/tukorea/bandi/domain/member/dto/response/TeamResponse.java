package kr.ac.tukorea.bandi.domain.member.dto.response;

import kr.ac.tukorea.bandi.domain.member.model.Team;

public record TeamResponse(
        Long teamId,
        String name,
        int displayOrder,
        boolean active
) {

    public static TeamResponse from(Team team) {
        return new TeamResponse(team.getTeamId(), team.getName(),
                team.getDisplayOrder(), team.isActive());
    }
}
