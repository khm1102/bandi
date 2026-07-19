package kr.ac.tukorea.bandi.domain.notice.model;

import java.util.Objects;

public enum InternalNoticeTargetScope {
    ALL,
    TEAM;

    public boolean matchesTeam(Long teamId) {
        return switch (this) {
            case ALL -> teamId == null;
            case TEAM -> teamId != null;
        };
    }

    public boolean isTeam(Long targetTeamId, Long memberTeamId) {
        return this == TEAM && Objects.equals(targetTeamId, memberTeamId);
    }
}
