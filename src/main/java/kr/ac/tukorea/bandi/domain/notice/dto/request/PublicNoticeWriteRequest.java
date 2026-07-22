package kr.ac.tukorea.bandi.domain.notice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PublicNoticeWriteRequest(
        @NotBlank @Size(max = 30) String categoryCode,
        @NotBlank @Size(max = 200) String title,
        @NotBlank String body,
        boolean pinned,
        List<@Positive Long> attachmentFileIds
) {

    public PublicNoticeWriteRequest {
        attachmentFileIds = attachmentFileIds == null
                ? List.of()
                : List.copyOf(attachmentFileIds);
    }

    public PublicNoticeWriteParam toParam() {
        return new PublicNoticeWriteParam(categoryCode, title, body, pinned,
                attachmentFileIds);
    }

    public PublicNoticeUpdateParam toUpdateParam(Long publicNoticeId) {
        return new PublicNoticeUpdateParam(publicNoticeId, categoryCode, title,
                body, pinned, attachmentFileIds);
    }
}
