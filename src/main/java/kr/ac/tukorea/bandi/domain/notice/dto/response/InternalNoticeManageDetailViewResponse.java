package kr.ac.tukorea.bandi.domain.notice.dto.response;

import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeStatus;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeTargetScope;
import kr.ac.tukorea.bandi.domain.notice.service.SafeMarkdownHtml;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter
public final class InternalNoticeManageDetailViewResponse {

    private final Long internalNoticeId;
    private final boolean teamNotice;
    private final String teamName;
    private final String title;
    private final SafeMarkdownHtml bodyHtml;
    private final String statusCode;
    private final String statusLabel;
    private final String statusTone;
    private final boolean important;
    private final String createdByName;
    private final String publishedByName;
    private final String updatedByName;
    private final String publishedAt;
    private final String updatedAt;
    private final boolean draft;
    private final boolean scheduled;
    private final boolean published;
    private final boolean closed;
    private final boolean archived;
    private final boolean readStatusAvailable;
    private final List<Attachment> attachments;

    private InternalNoticeManageDetailViewResponse(
            InternalNoticeManageDetailResponse response,
            DateTimeFormatter formatter) {
        InternalNoticeStatus status = response.status();
        this.internalNoticeId = response.internalNoticeId();
        this.teamNotice = response.targetScope() == InternalNoticeTargetScope.TEAM;
        this.teamName = response.teamName();
        this.title = response.title();
        this.bodyHtml = response.bodyHtml();
        this.statusCode = status.name();
        this.statusLabel = statusLabel(status);
        this.statusTone = statusTone(status);
        this.important = response.important();
        this.createdByName = response.createdByName();
        this.publishedByName = response.publishedByName();
        this.updatedByName = response.updatedByName();
        this.publishedAt = format(response.publishStartDttm(), formatter);
        this.updatedAt = format(response.updatedDttm(), formatter);
        this.draft = status == InternalNoticeStatus.DRAFT;
        this.scheduled = status == InternalNoticeStatus.SCHEDULED;
        this.published = status == InternalNoticeStatus.PUBLISHED;
        this.closed = status == InternalNoticeStatus.CLOSED;
        this.archived = status == InternalNoticeStatus.ARCHIVED;
        this.readStatusAvailable = response.publishedByName() != null
                && (published || closed || archived);
        this.attachments = response.attachments().stream().map(Attachment::from).toList();
    }

    public static InternalNoticeManageDetailViewResponse from(
            InternalNoticeManageDetailResponse response,
            DateTimeFormatter formatter) {
        return new InternalNoticeManageDetailViewResponse(response, formatter);
    }

    private static String format(LocalDateTime value, DateTimeFormatter formatter) {
        return value == null ? "" : formatter.format(value);
    }

    private static String statusLabel(InternalNoticeStatus status) {
        return switch (status) {
            case DRAFT -> "초안";
            case SCHEDULED -> "예약";
            case PUBLISHED -> "게시 중";
            case CLOSED -> "게시 종료";
            case ARCHIVED -> "보관";
        };
    }

    private static String statusTone(InternalNoticeStatus status) {
        return switch (status) {
            case DRAFT -> "neutral";
            case SCHEDULED -> "info";
            case PUBLISHED -> "success";
            case CLOSED -> "warning";
            case ARCHIVED -> "neutral";
        };
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
