package kr.ac.tukorea.bandi.domain.resource.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ResourceWriteRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank String bodyMarkdown,
        List<Long> attachmentFileIds
) {
    public ResourceWriteRequest {
        attachmentFileIds = attachmentFileIds == null ? List.of() : List.copyOf(attachmentFileIds);
    }
}
