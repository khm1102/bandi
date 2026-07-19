package kr.ac.tukorea.bandi.domain.performance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PublicProfileUpdateRequest(
        @NotBlank @Size(max = 100) String publicName,
        String bio,
        @Positive Long profileFileId,
        @Size(max = 500) String socialUrl
) {
    public PublicProfileUpdateParam toParam(Long publicProfileId) {
        return new PublicProfileUpdateParam(publicProfileId, publicName,
                bio, profileFileId, socialUrl);
    }
}
