package kr.ac.tukorea.bandi.domain.performance.dto.request;

public record PublicProfileCreateParam(
        Long memberId,
        String publicName,
        String bio,
        Long profileFileId,
        String socialUrl
) {
}
