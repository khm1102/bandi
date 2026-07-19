package kr.ac.tukorea.bandi.domain.resource.model;

import kr.ac.tukorea.bandi.domain.resource.exception.InvalidResourceException;
import kr.ac.tukorea.bandi.domain.resource.exception.InvalidResourceStateException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Resource {

    private static final int MAX_CATEGORY_LENGTH = 30;
    private static final int MAX_TITLE_LENGTH = 200;

    private Long resourceId;
    private final ResourceTargetScope targetScope;
    private final Long teamId;
    private final String categoryCode;
    private final String title;
    private final String description;
    private final ResourceStatus status;
    private final boolean pinned;
    private final Long createdByMemberId;
    private final Long updatedByMemberId;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;
    private final LocalDateTime deletedDttm;

    public Resource(Long resourceId, ResourceTargetScope targetScope, Long teamId,
                    String categoryCode, String title, String description,
                    ResourceStatus status, boolean pinned, Long createdByMemberId,
                    Long updatedByMemberId, LocalDateTime createdDttm,
                    LocalDateTime updatedDttm, LocalDateTime deletedDttm) {
        validate(targetScope, teamId, categoryCode, title, description, status,
                createdByMemberId, updatedByMemberId);
        this.resourceId = resourceId;
        this.targetScope = targetScope;
        this.teamId = teamId;
        this.categoryCode = categoryCode.strip();
        this.title = title.strip();
        this.description = description.strip();
        this.status = status;
        this.pinned = pinned;
        this.createdByMemberId = createdByMemberId;
        this.updatedByMemberId = updatedByMemberId;
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
        this.deletedDttm = deletedDttm;
    }

    public static Resource draft(ResourceTargetScope targetScope, Long teamId,
                                 String categoryCode, String title, String description,
                                 boolean pinned, Long actorMemberId) {
        return new Resource(null, targetScope, teamId, categoryCode, title, description,
                ResourceStatus.DRAFT, pinned, actorMemberId, actorMemberId,
                null, null, null);
    }

    public Resource edit(ResourceTargetScope newTargetScope, Long newTeamId,
                         String newCategoryCode, String newTitle,
                         String newDescription, boolean newPinned,
                         Long actorMemberId) {
        if (!status.canEdit()) {
            throw new InvalidResourceStateException(status);
        }
        return copy(newTargetScope, newTeamId, newCategoryCode, newTitle,
                newDescription, status, newPinned, actorMemberId);
    }

    public Resource publish(Long actorMemberId) {
        if (!status.canPublish()) {
            throw new InvalidResourceStateException(status);
        }
        return copy(targetScope, teamId, categoryCode, title, description,
                ResourceStatus.PUBLISHED, pinned, actorMemberId);
    }

    public Resource archive(Long actorMemberId) {
        if (!status.canArchive()) {
            throw new InvalidResourceStateException(status);
        }
        return copy(targetScope, teamId, categoryCode, title, description,
                ResourceStatus.ARCHIVED, pinned, actorMemberId);
    }

    public boolean isReadable() {
        return status.isReadable();
    }

    private Resource copy(ResourceTargetScope newTargetScope, Long newTeamId,
                          String newCategoryCode, String newTitle,
                          String newDescription, ResourceStatus newStatus,
                          boolean newPinned, Long actorMemberId) {
        return new Resource(resourceId, newTargetScope, newTeamId, newCategoryCode,
                newTitle, newDescription, newStatus, newPinned, createdByMemberId,
                actorMemberId, createdDttm, updatedDttm, deletedDttm);
    }

    private void validate(ResourceTargetScope scope, Long targetTeamId,
                          String category, String resourceTitle,
                          String resourceDescription, ResourceStatus resourceStatus,
                          Long creatorId, Long updaterId) {
        if (scope == null || !scope.matchesTeam(targetTeamId)) {
            throw new InvalidResourceException("target");
        }
        validateText(category, MAX_CATEGORY_LENGTH, "category");
        validateText(resourceTitle, MAX_TITLE_LENGTH, "title");
        validateText(resourceDescription, Integer.MAX_VALUE, "description");
        if (resourceStatus == null || creatorId == null || updaterId == null) {
            throw new InvalidResourceException("state-actor");
        }
    }

    private void validateText(String value, int maxLength, String field) {
        if (value == null || value.isBlank() || value.length() > maxLength) {
            throw new InvalidResourceException(field);
        }
    }
}
