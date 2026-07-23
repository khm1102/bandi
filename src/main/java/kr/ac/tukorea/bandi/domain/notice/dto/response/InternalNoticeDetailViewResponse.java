package kr.ac.tukorea.bandi.domain.notice.dto.response;

import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeTargetScope;
import kr.ac.tukorea.bandi.domain.notice.service.SafeMarkdownHtml;
import lombok.Getter;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
public final class InternalNoticeDetailViewResponse {

    private final Long internalNoticeId;
    private final boolean teamNotice;
    private final String teamName;
    private final String title;
    private final SafeMarkdownHtml bodyHtml;
    private final boolean important;
    private final String publishedByName;
    private final String publishedAt;
    private final String updatedAt;
    private final List<Attachment> attachments;

    private InternalNoticeDetailViewResponse(Long internalNoticeId, boolean teamNotice,
                                             String teamName, String title,
                                             SafeMarkdownHtml bodyHtml, boolean important,
                                             String publishedByName, String publishedAt,
                                             String updatedAt, List<Attachment> attachments) {
        this.internalNoticeId = internalNoticeId;
        this.teamNotice = teamNotice;
        this.teamName = teamName;
        this.title = title;
        this.bodyHtml = bodyHtml;
        this.important = important;
        this.publishedByName = publishedByName;
        this.publishedAt = publishedAt;
        this.updatedAt = updatedAt;
        this.attachments = List.copyOf(attachments);
    }

    public static InternalNoticeDetailViewResponse from(InternalNoticeDetailResponse response,
                                                          DateTimeFormatter dateTimeFormatter) {
        return new InternalNoticeDetailViewResponse(response.internalNoticeId(),
                response.targetScope() == InternalNoticeTargetScope.TEAM, response.teamName(), response.title(),
                response.bodyHtml(), response.important(), response.publishedByName(),
                dateTimeFormatter.format(response.publishStartDttm()),
                response.updatedDttm() == null ? "" : dateTimeFormatter.format(response.updatedDttm()),
                response.attachments().stream().map(Attachment::from).toList());
    }

    @Getter
    public static final class Attachment {

        private final Long storedFileId;
        private final String originalName;

        private Attachment(Long storedFileId, String originalName) {
            this.storedFileId = storedFileId;
            this.originalName = originalName;
        }

        private static Attachment from(InternalNoticeAttachmentResponse response) {
            return new Attachment(response.storedFileId(), response.originalName());
        }
    }
}
