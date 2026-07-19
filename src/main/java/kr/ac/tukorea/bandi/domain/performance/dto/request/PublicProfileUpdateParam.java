package kr.ac.tukorea.bandi.domain.performance.dto.request;

public record PublicProfileUpdateParam(
        Long publicProfileId,
        String publicName,
        String bio,
        Long profileFileId,
        String socialUrl
) {
}
