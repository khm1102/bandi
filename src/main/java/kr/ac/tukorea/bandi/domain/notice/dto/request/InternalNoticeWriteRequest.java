package kr.ac.tukorea.bandi.domain.notice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import kr.ac.tukorea.bandi.domain.notice.model.InternalNoticeTargetScope;

import java.util.List;

public record InternalNoticeWriteRequest(
        @NotNull InternalNoticeTargetScope targetScope,
        @Positive Long teamId,
        @NotBlank @Size(max = 200) String title,
        @NotBlank String body,
        boolean important,
        List<@Positive Long> attachmentFileIds
) {

    public InternalNoticeWriteRequest {
        attachmentFileIds = attachmentFileIds == null
                ? List.of()
                : List.copyOf(attachmentFileIds);
    }

    public InternalNoticeWriteParam toParam() {
        return new InternalNoticeWriteParam(targetScope, teamId, title, body,
                important, attachmentFileIds);
    }

    public InternalNoticeUpdateParam toUpdateParam(Long internalNoticeId) {
        return new InternalNoticeUpdateParam(internalNoticeId, targetScope,
                teamId, title, body, important, attachmentFileIds);
    }
}
