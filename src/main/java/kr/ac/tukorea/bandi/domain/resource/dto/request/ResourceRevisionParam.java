package kr.ac.tukorea.bandi.domain.resource.dto.request;

import java.util.List;

public record ResourceRevisionParam(
        Long resourceId,
        List<Long> storedFileIds
) {

    public ResourceRevisionParam {
        storedFileIds = storedFileIds == null ? List.of() : List.copyOf(storedFileIds);
    }
}
