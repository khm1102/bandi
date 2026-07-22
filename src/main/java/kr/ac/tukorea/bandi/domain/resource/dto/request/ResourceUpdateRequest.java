package kr.ac.tukorea.bandi.domain.resource.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import kr.ac.tukorea.bandi.domain.resource.model.ResourceTargetScope;

public record ResourceUpdateRequest(
        @NotNull ResourceTargetScope targetScope,
        @Positive Long teamId,
        @NotBlank @Size(max = 30) String categoryCode,
        @NotBlank @Size(max = 200) String title,
        @NotBlank String description,
        boolean pinned
) {

    public ResourceUpdateParam toParam(Long resourceId) {
        return new ResourceUpdateParam(resourceId, targetScope, teamId,
                categoryCode, title, description, pinned);
    }
}
