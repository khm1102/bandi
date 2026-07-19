package kr.ac.tukorea.bandi.domain.activity.model;

public enum ActivityRecordStatus {
    DRAFT,
    SUBMITTED,
    APPROVED,
    REVISION_REQUESTED,
    ARCHIVED;

    public boolean canEdit() {
        return this == DRAFT || this == REVISION_REQUESTED;
    }

    public boolean canSubmit() {
        return this == DRAFT || this == REVISION_REQUESTED;
    }

    public boolean canReview() {
        return this == SUBMITTED;
    }

    public boolean canArchive() {
        return this != ARCHIVED;
    }

    public boolean isReadable() {
        return this == APPROVED;
    }
}
