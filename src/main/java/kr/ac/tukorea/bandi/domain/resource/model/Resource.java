package kr.ac.tukorea.bandi.domain.resource.model;

import kr.ac.tukorea.bandi.domain.resource.exception.InvalidResourceException;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Resource {

    private static final int MAX_TITLE_LENGTH = 200;

    private final Long resourceId;
    private final String title;
    private final String bodyMarkdown;
    private final Long createdByMemberId;
    private final Long updatedByMemberId;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;
    private final LocalDateTime deletedDttm;

    public Resource(Long resourceId, String title, String bodyMarkdown,
                    Long createdByMemberId, Long updatedByMemberId,
                    LocalDateTime createdDttm, LocalDateTime updatedDttm,
                    LocalDateTime deletedDttm) {
        validate(title, bodyMarkdown, createdByMemberId, updatedByMemberId);
        this.resourceId = resourceId;
        this.title = title.strip();
        this.bodyMarkdown = bodyMarkdown.strip();
        this.createdByMemberId = createdByMemberId;
        this.updatedByMemberId = updatedByMemberId;
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
        this.deletedDttm = deletedDttm;
    }

    public static Resource create(String title, String bodyMarkdown, Long actorMemberId) {
        return new Resource(null, title, bodyMarkdown, actorMemberId, actorMemberId,
                null, null, null);
    }

    public Resource edit(String newTitle, String newBodyMarkdown, Long actorMemberId) {
        return new Resource(resourceId, newTitle, newBodyMarkdown, createdByMemberId,
                actorMemberId, createdDttm, updatedDttm, deletedDttm);
    }

    public boolean isCreatedBy(Long memberId) {
        return createdByMemberId.equals(memberId);
    }

    private void validate(String resourceTitle, String markdown, Long creatorId, Long updaterId) {
        if (resourceTitle == null || resourceTitle.isBlank()
                || resourceTitle.strip().length() > MAX_TITLE_LENGTH) {
            throw new InvalidResourceException("title");
        }
        if (markdown == null || markdown.isBlank()) {
            throw new InvalidResourceException("body");
        }
        if (creatorId == null || updaterId == null) {
            throw new InvalidResourceException("actor");
        }
    }
}
