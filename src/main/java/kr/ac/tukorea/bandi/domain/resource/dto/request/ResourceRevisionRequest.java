package kr.ac.tukorea.bandi.domain.resource.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record ResourceRevisionRequest(
        @NotEmpty List<@Positive Long> storedFileIds
) {

    public ResourceRevisionParam toParam(Long resourceId) {
        return new ResourceRevisionParam(resourceId, storedFileIds);
    }
}
