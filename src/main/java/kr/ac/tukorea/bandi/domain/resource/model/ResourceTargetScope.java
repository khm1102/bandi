package kr.ac.tukorea.bandi.domain.resource.model;

public enum ResourceTargetScope {
    ALL,
    TEAM;

    public boolean matchesTeam(Long teamId) {
        return switch (this) {
            case ALL -> teamId == null;
            case TEAM -> teamId != null;
        };
    }
}
