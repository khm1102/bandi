package kr.ac.tukorea.bandi.domain.notice.dto.response;

import kr.ac.tukorea.bandi.domain.notice.service.SafeMarkdownHtml;

public record MarkdownPreviewResponse(SafeMarkdownHtml html) {
}
