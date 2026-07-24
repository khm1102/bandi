package kr.ac.tukorea.bandi.domain.resource.service;

import kr.ac.tukorea.bandi.domain.file.service.FileService;
import kr.ac.tukorea.bandi.domain.file.service.FileUploadParam;
import kr.ac.tukorea.bandi.domain.resource.mapper.ResourceMapper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class ResourceLinkPreviewFetcher {

    private static final int MAX_PREVIEWS = 5;
    private static final int MAX_REDIRECTS = 3;
    private static final int HTML_MAX_BYTES = 512 * 1024;
    private static final int IMAGE_MAX_BYTES = 5 * 1024 * 1024;

    private final FileService fileService;

    public ResourceLinkPreviewFetcher(FileService fileService) {
        this.fileService = fileService;
    }

    public Map<String, ResourceMapper.ResourceLinkPreviewRow> fetchAll(Long resourceId,
                                                                         Long memberId,
                                                                         Set<String> urls) {
        Map<String, ResourceMapper.ResourceLinkPreviewRow> previews = new LinkedHashMap<>();
        urls.stream().limit(MAX_PREVIEWS)
                .forEach(url -> fetch(resourceId, memberId, url)
                        .ifPresent(row -> previews.put(url, row)));
        return previews;
    }

    private Optional<ResourceMapper.ResourceLinkPreviewRow> fetch(Long resourceId,
                                                                    Long memberId,
                                                                    String value) {
        try {
            URI requestedUri = URI.create(value);
            Connection.Response response = execute(requestedUri, HTML_MAX_BYTES, false);
            String contentType = response.contentType();
            if (contentType == null || (!contentType.startsWith("text/html")
                    && !contentType.startsWith("application/xhtml+xml"))) {
                return Optional.empty();
            }
            Document document = response.parse();
            URI resolvedUri = URI.create(response.url().toExternalForm());
            String title = meta(document, "meta[property=og:title]", document.title());
            String description = meta(document, "meta[property=og:description]",
                    meta(document, "meta[name=description]", null));
            Long previewImageFileId = uploadPreviewImage(memberId, resolvedUri,
                    meta(document, "meta[property=og:image]", null));
            String normalizedUrl = resolvedUri.normalize().toString();
            return Optional.of(new ResourceMapper.ResourceLinkPreviewRow(resourceId,
                    normalizedUrl, hash(normalizedUrl), resolvedUri.getHost(), title,
                    description, previewImageFileId, LocalDateTime.now()));
        } catch (RuntimeException | IOException exception) {
            return Optional.empty();
        }
    }

    private Long uploadPreviewImage(Long memberId, URI pageUri, String imageValue) {
        if (imageValue == null || imageValue.isBlank()) {
            return null;
        }
        try {
            URI imageUri = pageUri.resolve(imageValue);
            Connection.Response response = execute(imageUri, IMAGE_MAX_BYTES, true);
            String contentType = response.contentType();
            String extension = extension(contentType);
            byte[] bytes = response.bodyAsBytes();
            if (extension == null || bytes.length == 0 || bytes.length > IMAGE_MAX_BYTES) {
                return null;
            }
            return fileService.uploadPrivate(new FileUploadParam("resource-link-preview",
                    "link-preview." + extension, bytes.length,
                    () -> new ByteArrayInputStream(bytes), memberId));
        } catch (RuntimeException | IOException exception) {
            return null;
        }
    }

    private Connection.Response execute(URI initialUri, int maxBodySize,
                                        boolean ignoreContentType) throws IOException {
        URI current = initialUri;
        for (int redirectCount = 0; redirectCount <= MAX_REDIRECTS; redirectCount += 1) {
            validate(current);
            Connection.Response response = Jsoup.connect(current.toString())
                    .followRedirects(false)
                    .timeout(3_000)
                    .maxBodySize(maxBodySize)
                    .userAgent("bandi-link-preview/1.0")
                    .ignoreContentType(ignoreContentType)
                    .execute();
            if (!isRedirect(response.statusCode())) {
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    throw new IllegalArgumentException("unexpected-status");
                }
                return response;
            }
            String location = response.header("Location");
            if (location == null || location.isBlank() || redirectCount == MAX_REDIRECTS) {
                throw new IllegalArgumentException("unsafe-redirect");
            }
            current = current.resolve(location);
        }
        throw new IllegalArgumentException("unsafe-redirect");
    }

    private boolean isRedirect(int statusCode) {
        return statusCode == 301 || statusCode == 302 || statusCode == 303
                || statusCode == 307 || statusCode == 308;
    }

    private void validate(URI uri) {
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                || uri.getUserInfo() != null) {
            throw new IllegalArgumentException("unsafe-url");
        }
        try {
            for (InetAddress address : InetAddress.getAllByName(uri.getHost())) {
                if (address.isAnyLocalAddress() || address.isLoopbackAddress()
                        || address.isLinkLocalAddress() || address.isSiteLocalAddress()
                        || address.isMulticastAddress()) {
                    throw new IllegalArgumentException("private-address");
                }
            }
        } catch (java.net.UnknownHostException exception) {
            throw new IllegalArgumentException("unknown-host", exception);
        }
    }

    private String meta(Document document, String selector, String fallback) {
        Element element = document.selectFirst(selector);
        if (element == null) {
            return fallback;
        }
        String value = element.attr("content").strip();
        return value.isBlank() ? fallback : value;
    }

    private String extension(String contentType) {
        if (contentType == null) {
            return null;
        }
        return switch (contentType.split(";", 2)[0].toLowerCase(java.util.Locale.ROOT)) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> null;
        };
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
