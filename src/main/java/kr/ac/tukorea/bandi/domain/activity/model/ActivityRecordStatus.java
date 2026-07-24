package kr.ac.tukorea.bandi.domain.activity.model;

public enum ActivityRecordStatus {
    DRAFT,
    SUBMITTED,
    TEAM_APPROVED,
    APPROVED,
    REVISION_REQUESTED,
    ARCHIVED;

    public boolean canEdit() {
        return this == DRAFT || this == REVISION_REQUESTED;
    }

    public boolean canSubmit() {
        return this == DRAFT || this == REVISION_REQUESTED;
    }

    public boolean canTeamApprove() {
        return this == SUBMITTED;
    }

    public boolean canFinalApprove() {
        return this == TEAM_APPROVED || this == SUBMITTED;
    }

    public boolean canRequestRevision() {
        return this == SUBMITTED || this == TEAM_APPROVED;
    }

    public boolean canArchive() {
        return this == APPROVED;
    }

    public boolean isReadable() {
        return this == APPROVED;
    }
}
