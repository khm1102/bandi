package kr.ac.tukorea.bandi.domain.performance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PublicProfileCreateRequest(
        @Positive Long memberId,
        @NotBlank @Size(max = 100) String publicName,
        String bio,
        @Positive Long profileFileId,
        @Size(max = 500) String socialUrl
) {
    public PublicProfileCreateParam toParam() {
        return new PublicProfileCreateParam(memberId, publicName, bio,
                profileFileId, socialUrl);
    }
}
