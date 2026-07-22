package kr.ac.tukorea.bandi.domain.notice.dto.request;

import jakarta.validation.constraints.NotBlank;

public record MarkdownPreviewRequest(@NotBlank String bodyMarkdown) {
}
