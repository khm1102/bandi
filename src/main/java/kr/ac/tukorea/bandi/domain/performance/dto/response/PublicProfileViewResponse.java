package kr.ac.tukorea.bandi.domain.performance.dto.response;

import kr.ac.tukorea.bandi.domain.performance.model.ConsentScope;
import kr.ac.tukorea.bandi.domain.performance.model.PublicProfile;

import java.util.Set;

public record PublicProfileViewResponse(
        Long publicProfileId,
        String publicName,
        String bio,
        Long profileFileId,
        String socialUrl
) {

    public static PublicProfileViewResponse from(
            PublicProfile profile, Set<ConsentScope> scopes) {
        return new PublicProfileViewResponse(profile.getPublicProfileId(),
                scopes.contains(ConsentScope.NAME)
                        ? profile.getPublicName() : null,
                scopes.contains(ConsentScope.BIO)
                        ? profile.getBio() : null,
                scopes.contains(ConsentScope.PHOTO)
                        ? profile.getProfileFileId() : null,
                scopes.contains(ConsentScope.SOCIAL)
                        ? profile.getSocialUrl() : null);
    }
}
