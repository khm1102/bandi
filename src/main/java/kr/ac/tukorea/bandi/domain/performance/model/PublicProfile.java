package kr.ac.tukorea.bandi.domain.performance.model;

import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPublicProfileException;
import lombok.Getter;

import java.net.URI;
import java.time.LocalDateTime;

@Getter
public class PublicProfile {

    private final Long publicProfileId;
    private final Long memberId;
    private final String publicName;
    private final String bio;
    private final Long profileFileId;
    private final String socialUrl;
    private final PublicProfileVisibility visibilityStatus;
    private final LocalDateTime createdDttm;
    private final LocalDateTime updatedDttm;

    public PublicProfile(Long publicProfileId, Long memberId,
                         String publicName, String bio, Long profileFileId,
                         String socialUrl,
                         PublicProfileVisibility visibilityStatus,
                         LocalDateTime createdDttm,
                         LocalDateTime updatedDttm) {
        this.publicProfileId = publicProfileId;
        this.memberId = memberId;
        this.publicName = requireText(publicName, 100, "publicName");
        this.bio = optionalText(bio, "bio");
        this.profileFileId = profileFileId;
        this.socialUrl = validateUrl(socialUrl);
        this.visibilityStatus = requireVisibility(visibilityStatus);
        this.createdDttm = createdDttm;
        this.updatedDttm = updatedDttm;
    }

    public static PublicProfile draft(Long memberId, String publicName,
                                      String bio, Long profileFileId,
                                      String socialUrl) {
        return new PublicProfile(null, memberId, publicName, bio,
                profileFileId, socialUrl, PublicProfileVisibility.DRAFT,
                null, null);
    }

    public PublicProfile edit(String publicName, String bio,
                              Long profileFileId, String socialUrl) {
        validateMutable();
        return new PublicProfile(publicProfileId, memberId, publicName,
                bio, profileFileId, socialUrl, visibilityStatus,
                createdDttm, updatedDttm);
    }

    public PublicProfile changeVisibility(
            PublicProfileVisibility newVisibility) {
        if (newVisibility == null || visibilityStatus == newVisibility) {
            throw new InvalidPublicProfileException("visibilityStatus");
        }
        if (visibilityStatus == PublicProfileVisibility.ARCHIVED) {
            throw new InvalidPublicProfileException("archived");
        }
        if (visibilityStatus == PublicProfileVisibility.DRAFT
                && newVisibility == PublicProfileVisibility.ARCHIVED) {
            throw new InvalidPublicProfileException("visibilityTransition");
        }
        return new PublicProfile(publicProfileId, memberId, publicName,
                bio, profileFileId, socialUrl, newVisibility,
                createdDttm, updatedDttm);
    }

    public void validateConsentScope(ConsentScope scope) {
        if (scope == null) {
            throw new InvalidPublicProfileException("consentScope");
        }
        boolean fieldExists = switch (scope) {
            case NAME -> true;
            case PHOTO -> profileFileId != null;
            case BIO -> bio != null;
            case SOCIAL -> socialUrl != null;
        };
        if (!fieldExists) {
            throw new InvalidPublicProfileException("consentField");
        }
    }

    private void validateMutable() {
        if (visibilityStatus == PublicProfileVisibility.ARCHIVED) {
            throw new InvalidPublicProfileException("archived");
        }
    }

    private static String validateUrl(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.strip();
        if (normalized.length() > 500) {
            throw new InvalidPublicProfileException("socialUrl");
        }
        try {
            URI uri = URI.create(normalized);
            String scheme = uri.getScheme();
            if (uri.getHost() == null
                    || !("http".equalsIgnoreCase(scheme)
                    || "https".equalsIgnoreCase(scheme))) {
                throw new InvalidPublicProfileException("socialUrl");
            }
        } catch (IllegalArgumentException exception) {
            throw new InvalidPublicProfileException("socialUrl");
        }
        return normalized;
    }

    private static String requireText(String value, int maxLength,
                                      String field) {
        if (value == null || value.isBlank()
                || value.strip().length() > maxLength) {
            throw new InvalidPublicProfileException(field);
        }
        return value.strip();
    }

    private static String optionalText(String value, String field) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (value.length() > 10_000) {
            throw new InvalidPublicProfileException(field);
        }
        return value;
    }

    private static PublicProfileVisibility requireVisibility(
            PublicProfileVisibility value) {
        if (value == null) {
            throw new InvalidPublicProfileException("visibilityStatus");
        }
        return value;
    }
}
