package kr.ac.tukorea.bandi.domain.member.service;

import kr.ac.tukorea.bandi.domain.member.model.ClubRole;
import kr.ac.tukorea.bandi.domain.member.model.Member;
import kr.ac.tukorea.bandi.domain.member.model.MemberStatus;

import java.util.Objects;

public record MemberAccessContext(
        Long memberId,
        Long teamId,
        boolean admin,
        boolean leader,
        boolean active
) {

    public static MemberAccessContext from(Member member) {
        return new MemberAccessContext(member.getMemberId(), member.getTeamId(),
                member.getRole() == ClubRole.ADMIN,
                member.getRole() == ClubRole.LEADER,
                member.getStatus() == MemberStatus.ACTIVE);
    }

    public boolean canReadInternal() {
        return active;
    }

    public boolean canManageGlobal() {
        return active && admin;
    }

    public boolean canManageTeam(Long targetTeamId) {
        if (!active || targetTeamId == null) {
            return false;
        }
        return admin || (leader && Objects.equals(teamId, targetTeamId));
    }
    public boolean canContributeToTeam(Long targetTeamId) {
        if (!active || targetTeamId == null) {
            return false;
        }
        return admin || Objects.equals(teamId, targetTeamId);
    }
}
