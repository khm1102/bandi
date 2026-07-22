package kr.ac.tukorea.bandi.domain.resource.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import kr.ac.tukorea.bandi.domain.resource.model.ResourceTargetScope;

import java.util.List;

public record ResourceCreateRequest(
        @NotNull ResourceTargetScope targetScope,
        @Positive Long teamId,
        @NotBlank @Size(max = 30) String categoryCode,
        @NotBlank @Size(max = 200) String title,
        @NotBlank String description,
        boolean pinned,
        List<@Positive Long> storedFileIds
) {

    public ResourceCreateRequest {
        storedFileIds = storedFileIds == null
                ? List.of()
                : List.copyOf(storedFileIds);
    }

    public ResourceWriteParam toParam() {
        return new ResourceWriteParam(targetScope, teamId, categoryCode, title,
                description, pinned, storedFileIds);
    }
}
