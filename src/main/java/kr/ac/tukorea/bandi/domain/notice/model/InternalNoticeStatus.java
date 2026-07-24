package kr.ac.tukorea.bandi.domain.notice.model;

public enum InternalNoticeStatus {
    DRAFT,
    SCHEDULED,
    PUBLISHED,
    CLOSED,
    ARCHIVED;

    public boolean canEdit() {
        return this == DRAFT || this == SCHEDULED || this == PUBLISHED;
    }

    public boolean canPublish() {
        return this == DRAFT || this == SCHEDULED || this == PUBLISHED;
    }

    public boolean canClose() {
        return this == SCHEDULED || this == PUBLISHED;
    }

    public boolean canArchive() {
        return this == CLOSED;
    }

    public boolean canReturnToDraft() {
        return this == SCHEDULED || this == ARCHIVED;
    }

    public boolean canDelete() {
        return this == DRAFT;
    }

    public boolean canBePublic() {
        return this == SCHEDULED || this == PUBLISHED;
    }
}
