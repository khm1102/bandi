package kr.ac.tukorea.bandi.domain.resource.model;

public enum ResourceStatus {
    DRAFT,
    PUBLISHED,
    ARCHIVED;

    public boolean canEdit() {
        return this == DRAFT || this == PUBLISHED;
    }

    public boolean canPublish() {
        return this == DRAFT;
    }

    public boolean canArchive() {
        return this != ARCHIVED;
    }

    public boolean isReadable() {
        return this == PUBLISHED;
    }
}
