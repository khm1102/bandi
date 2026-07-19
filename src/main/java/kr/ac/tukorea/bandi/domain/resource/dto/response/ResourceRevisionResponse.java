package kr.ac.tukorea.bandi.domain.resource.dto.response;

import java.util.List;

public record ResourceRevisionResponse(
        int revisionNo,
        List<ResourceFileResponse> files
) {

    public ResourceRevisionResponse {
        files = List.copyOf(files);
    }
}
