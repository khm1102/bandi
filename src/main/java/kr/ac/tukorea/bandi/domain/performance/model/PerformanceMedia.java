package kr.ac.tukorea.bandi.domain.performance.model;

import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPerformanceContentException;
import lombok.Getter;

import java.net.URI;

@Getter
public class PerformanceMedia {

    private Long performanceMediaId;
    private final Long performanceProjectId;
    private final Long storedFileId;
    private final MediaType mediaType;
    private final String title;
    private final String description;
    private final String altText;
    private final String creditText;
    private final String externalUrl;
    private final int displayOrder;
    private final boolean published;

    public PerformanceMedia(Long performanceMediaId,
                            Long performanceProjectId, Long storedFileId,
                            MediaType mediaType, String title,
                            String description, String altText,
                            String creditText, String externalUrl,
                            Integer displayOrder, boolean published) {
        this.performanceMediaId = performanceMediaId;
        this.performanceProjectId = requireId(
                performanceProjectId, "performanceProjectId");
        this.storedFileId = requireId(storedFileId, "storedFileId");
        this.mediaType = requireType(mediaType);
        this.title = requireText(title, 200, "title");
        this.description = requireText(
                description, 10_000, "description");
        this.altText = requireText(altText, 500, "altText");
        this.creditText = requireText(creditText, 500, "creditText");
        this.externalUrl = validateUrl(externalUrl);
        this.displayOrder = requireOrder(displayOrder);
        this.published = published;
    }

    public static PerformanceMedia create(
            Long performanceProjectId, Long storedFileId,
            MediaType mediaType, String title, String description,
            String altText, String creditText, String externalUrl,
            int displayOrder) {
        return new PerformanceMedia(null, performanceProjectId,
                storedFileId, mediaType, title, description, altText,
                creditText, externalUrl, displayOrder, false);
    }

    public PerformanceMedia edit(MediaType mediaType, String title,
                                 String description, String altText,
                                 String creditText, String externalUrl,
                                 int displayOrder) {
        return new PerformanceMedia(performanceMediaId,
                performanceProjectId, storedFileId, mediaType, title,
                description, altText, creditText, externalUrl,
                displayOrder, published);
    }

    public PerformanceMedia changePublished(boolean published) {
        if (this.published == published) {
            throw new InvalidPerformanceContentException("published");
        }
        return new PerformanceMedia(performanceMediaId,
                performanceProjectId, storedFileId, mediaType, title,
                description, altText, creditText, externalUrl,
                displayOrder, published);
    }

    public void validateFile(Long storedFileId) {
        if (!this.storedFileId.equals(storedFileId)) {
            throw new InvalidPerformanceContentException("storedFileId");
        }
    }

    private static String validateUrl(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.strip();
        if (normalized.length() > 1_000) {
            throw new InvalidPerformanceContentException("externalUrl");
        }
        try {
            URI uri = URI.create(normalized);
            String scheme = uri.getScheme();
            if (uri.getHost() == null
                    || !("http".equalsIgnoreCase(scheme)
                    || "https".equalsIgnoreCase(scheme))) {
                throw new InvalidPerformanceContentException("externalUrl");
            }
        } catch (IllegalArgumentException exception) {
            throw new InvalidPerformanceContentException("externalUrl");
        }
        return normalized;
    }

    private static Long requireId(Long value, String field) {
        if (value == null || value < 1) {
            throw new InvalidPerformanceContentException(field);
        }
        return value;
    }

    private static MediaType requireType(MediaType value) {
        if (value == null) {
            throw new InvalidPerformanceContentException("mediaType");
        }
        return value;
    }

    private static String requireText(String value, int maxLength,
                                      String field) {
        if (value == null || value.isBlank()
                || value.strip().length() > maxLength) {
            throw new InvalidPerformanceContentException(field);
        }
        return value.strip();
    }

    private static int requireOrder(Integer value) {
        if (value == null || value < 0) {
            throw new InvalidPerformanceContentException("displayOrder");
        }
        return value;
    }
}
