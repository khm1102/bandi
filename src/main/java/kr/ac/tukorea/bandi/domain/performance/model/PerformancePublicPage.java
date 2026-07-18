package kr.ac.tukorea.bandi.domain.performance.model;

import kr.ac.tukorea.bandi.domain.performance.exception.InvalidPerformancePublicPageException;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

@Getter
public class PerformancePublicPage {

    private static final Pattern SLUG_PATTERN = Pattern.compile(
            "^[a-z0-9]+(?:-[a-z0-9]+)*$");
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile(
            "^#[0-9A-Fa-f]{6}$");

    private Long performancePublicPageId;
    private final Long performanceProjectId;
    private final String slug;
    private final PublicPageStatus status;
    private final String shortDescription;
    private final String synopsis;
    private final String directorNote;
    private final String genre;
    private final String ageRating;
    private final int runtimeMinutes;
    private final Integer intermissionMinutes;
    private final long admissionFee;
    private final Long heroFileId;
    private final Long posterFileId;
    private final String accentColor;
    private final String contactName;
    private final String contactChannel;
    private final String organizerName;
    private final String ogTitle;
    private final String ogDescription;
    private final Long ogImageFileId;
    private final LocalDateTime publishStartDttm;
    private final LocalDateTime publishEndDttm;

    public PerformancePublicPage(
            Long performancePublicPageId, Long performanceProjectId,
            String slug, PublicPageStatus status,
            String shortDescription, String synopsis, String directorNote,
            String genre, String ageRating, Integer runtimeMinutes,
            Integer intermissionMinutes, Long admissionFee,
            Long heroFileId, Long posterFileId, String accentColor,
            String contactName, String contactChannel, String organizerName,
            String ogTitle, String ogDescription, Long ogImageFileId,
            LocalDateTime publishStartDttm,
            LocalDateTime publishEndDttm) {
        this.performancePublicPageId = performancePublicPageId;
        this.performanceProjectId = requireId(
                performanceProjectId, "performanceProjectId");
        this.slug = validateSlug(slug);
        this.status = requireStatus(status);
        this.shortDescription = requireText(
                shortDescription, 2_000, "shortDescription");
        this.synopsis = requireText(synopsis, 100_000, "synopsis");
        this.directorNote = optionalText(directorNote, 10_000,
                "directorNote");
        this.genre = requireText(genre, 100, "genre");
        this.ageRating = requireText(ageRating, 100, "ageRating");
        this.runtimeMinutes = requirePositive(runtimeMinutes,
                "runtimeMinutes");
        this.intermissionMinutes = validateNonNegative(
                intermissionMinutes, "intermissionMinutes");
        this.admissionFee = requireNonNegative(admissionFee,
                "admissionFee");
        this.heroFileId = heroFileId;
        this.posterFileId = posterFileId;
        this.accentColor = validateAccentColor(accentColor);
        this.contactName = requireText(contactName, 100, "contactName");
        this.contactChannel = requireText(
                contactChannel, 500, "contactChannel");
        this.organizerName = requireText(
                organizerName, 200, "organizerName");
        this.ogTitle = optionalText(ogTitle, 200, "ogTitle");
        this.ogDescription = optionalText(
                ogDescription, 500, "ogDescription");
        this.ogImageFileId = ogImageFileId;
        this.publishStartDttm = publishStartDttm;
        this.publishEndDttm = publishEndDttm;
        validatePublicationWindow();
    }

    public static PerformancePublicPage draft(
            Long performanceProjectId, String slug,
            String shortDescription, String synopsis, String directorNote,
            String genre, String ageRating, int runtimeMinutes,
            Integer intermissionMinutes, long admissionFee,
            Long heroFileId, Long posterFileId, String accentColor,
            String contactName, String contactChannel, String organizerName,
            String ogTitle, String ogDescription, Long ogImageFileId,
            LocalDateTime publishStartDttm,
            LocalDateTime publishEndDttm) {
        return new PerformancePublicPage(null, performanceProjectId, slug,
                PublicPageStatus.DRAFT, shortDescription, synopsis,
                directorNote, genre, ageRating, runtimeMinutes,
                intermissionMinutes, admissionFee, heroFileId, posterFileId,
                accentColor, contactName, contactChannel, organizerName,
                ogTitle, ogDescription, ogImageFileId, publishStartDttm,
                publishEndDttm);
    }

    public PerformancePublicPage edit(
            String slug, String shortDescription, String synopsis,
            String directorNote, String genre, String ageRating,
            int runtimeMinutes, Integer intermissionMinutes,
            long admissionFee, Long heroFileId, Long posterFileId,
            String accentColor, String contactName, String contactChannel,
            String organizerName, String ogTitle, String ogDescription,
            Long ogImageFileId, LocalDateTime publishStartDttm,
            LocalDateTime publishEndDttm) {
        validateMutable();
        return new PerformancePublicPage(performancePublicPageId,
                performanceProjectId, slug, status, shortDescription,
                synopsis, directorNote, genre, ageRating, runtimeMinutes,
                intermissionMinutes, admissionFee, heroFileId, posterFileId,
                accentColor, contactName, contactChannel, organizerName,
                ogTitle, ogDescription, ogImageFileId, publishStartDttm,
                publishEndDttm);
    }

    public PerformancePublicPage changeStatus(PublicPageStatus newStatus) {
        if (newStatus == null || status == newStatus
                || !allowedNextStatuses().contains(newStatus)) {
            throw new InvalidPerformancePublicPageException("status");
        }
        return new PerformancePublicPage(performancePublicPageId,
                performanceProjectId, slug, newStatus, shortDescription,
                synopsis, directorNote, genre, ageRating, runtimeMinutes,
                intermissionMinutes, admissionFee, heroFileId, posterFileId,
                accentColor, contactName, contactChannel, organizerName,
                ogTitle, ogDescription, ogImageFileId, publishStartDttm,
                publishEndDttm);
    }

    public void validateProject(Long performanceProjectId) {
        if (!Objects.equals(this.performanceProjectId,
                performanceProjectId)) {
            throw new InvalidPerformancePublicPageException(
                    "performanceProjectId");
        }
    }

    private Set<PublicPageStatus> allowedNextStatuses() {
        return switch (status) {
            case DRAFT -> EnumSet.of(PublicPageStatus.SCHEDULED,
                    PublicPageStatus.PUBLISHED,
                    PublicPageStatus.CANCELLED);
            case SCHEDULED -> EnumSet.of(PublicPageStatus.DRAFT,
                    PublicPageStatus.PUBLISHED,
                    PublicPageStatus.CANCELLED);
            case PUBLISHED -> EnumSet.of(PublicPageStatus.ENDED,
                    PublicPageStatus.CANCELLED);
            case ENDED, CANCELLED -> EnumSet.of(PublicPageStatus.ARCHIVED);
            case ARCHIVED -> EnumSet.noneOf(PublicPageStatus.class);
        };
    }

    private void validatePublicationWindow() {
        if (status == PublicPageStatus.SCHEDULED
                && publishStartDttm == null) {
            throw new InvalidPerformancePublicPageException(
                    "publishStartDttm");
        }
        if (publishStartDttm != null && publishEndDttm != null
                && !publishEndDttm.isAfter(publishStartDttm)) {
            throw new InvalidPerformancePublicPageException(
                    "publishEndDttm");
        }
    }

    private void validateMutable() {
        if (status == PublicPageStatus.ARCHIVED) {
            throw new InvalidPerformancePublicPageException("archived");
        }
    }

    private static String validateSlug(String value) {
        String slug = requireText(value, 150, "slug");
        if (slug.length() < 3 || !SLUG_PATTERN.matcher(slug).matches()) {
            throw new InvalidPerformancePublicPageException("slug");
        }
        return slug;
    }

    private static String validateAccentColor(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String color = value.strip().toUpperCase();
        if (!HEX_COLOR_PATTERN.matcher(color).matches()) {
            throw new InvalidPerformancePublicPageException("accentColor");
        }
        return color;
    }

    private static String requireText(String value, int maxLength,
                                      String field) {
        if (value == null || value.isBlank()
                || value.strip().length() > maxLength) {
            throw new InvalidPerformancePublicPageException(field);
        }
        return value.strip();
    }

    private static String optionalText(String value, int maxLength,
                                       String field) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (value.length() > maxLength) {
            throw new InvalidPerformancePublicPageException(field);
        }
        return value;
    }

    private static Long requireId(Long value, String field) {
        if (value == null || value < 1) {
            throw new InvalidPerformancePublicPageException(field);
        }
        return value;
    }

    private static PublicPageStatus requireStatus(PublicPageStatus value) {
        if (value == null) {
            throw new InvalidPerformancePublicPageException("status");
        }
        return value;
    }

    private static int requirePositive(Integer value, String field) {
        if (value == null || value < 1) {
            throw new InvalidPerformancePublicPageException(field);
        }
        return value;
    }

    private static Integer validateNonNegative(Integer value, String field) {
        if (value != null && value < 0) {
            throw new InvalidPerformancePublicPageException(field);
        }
        return value;
    }

    private static long requireNonNegative(Long value, String field) {
        if (value == null || value < 0) {
            throw new InvalidPerformancePublicPageException(field);
        }
        return value;
    }
}
