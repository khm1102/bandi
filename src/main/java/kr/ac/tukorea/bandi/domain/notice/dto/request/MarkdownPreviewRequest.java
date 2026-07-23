package kr.ac.tukorea.bandi.domain.notice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record MarkdownPreviewRequest(
        @Positive Long internalNoticeId,
        @NotBlank String bodyMarkdown,
        List<@Positive Long> attachmentFileIds
) {

    public MarkdownPreviewRequest {
        attachmentFileIds = attachmentFileIds == null ? List.of() : List.copyOf(attachmentFileIds);
    }
}
